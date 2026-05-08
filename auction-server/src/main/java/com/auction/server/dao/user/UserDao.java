package com.auction.server.dao.user;

import com.auction.server.dao.platform.BaseDao;

import com.auction.shared.*;
import java.sql.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao extends BaseDao<User> implements UserRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

  @Override
  protected User mapRow(ResultSet rs) throws SQLException {
    String role = rs.getString("role");
    User user = role.equalsIgnoreCase("ADMIN") ? new Admin() : role.equalsIgnoreCase("SELLER") ? new Seller() : new Bidder();
    user.setId(rs.getInt("id")); user.setVersion(rs.getInt("version"));
    user.setUsername(rs.getString("username")); user.setFullName(rs.getString("fullname"));
    user.setPassword(rs.getString("password")); user.setEmail(rs.getString("email"));
    user.setAge(rs.getString("age")); user.setPhoneNumber(rs.getString("phonenumber"));
    user.setBalance(rs.getDouble("balance")); user.setMoneySpent(rs.getDouble("moneyspent"));
    user.setItemsBought(rs.getInt("itemsbought")); user.setMoneyReceived(rs.getDouble("moneyreceived"));
    user.setItemsSold(rs.getInt("itemssold")); user.setActive(rs.getBoolean("isactive"));
    user.setLocked(rs.getBoolean("islocked")); user.setAvatarUrl(rs.getString("avatar_url"));
    try { user.setAvgRating(rs.getDouble("avgrating")); } catch (Exception e) {}
    try { user.setTotalRatings(rs.getInt("totalratings")); } catch (Exception e) {}
    return user;
  }

  @Override
  public User login(String username, String password) {
    return querySingle(
        "select * from users where username = ? and password = ? and isactive = true and islocked = false",
        username, password);
  }

  @Override
  public boolean signup(User user) {
    try (Connection conn = getConn()) {
      String username = normalize(user.getUsername());
      String email = normalize(user.getEmail());
      if (existsDuplicateUser(conn, username, email)) return false;
      String sql = "insert into users(username, fullname, password, email, age, phonenumber, role, isactive, islocked) values(?,?,?,?,?,?,?,?,?)";
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        String profileName = normalize(user.getFullName());
        if (profileName.isBlank()) profileName = username;
        ps.setString(1, username); ps.setString(2, profileName); ps.setString(3, user.getPassword());
        ps.setString(4, email); ps.setString(5, user.getAge()); ps.setString(6, user.getPhoneNumber());
        ps.setString(7, user.getRole().name()); ps.setBoolean(8, true); ps.setBoolean(9, false);
        return ps.executeUpdate() > 0;
      }
    } catch (SQLIntegrityConstraintViolationException e) { return false; }
    catch (Exception e) { LOGGER.warn("Signup failed", e); return false; }
  }

  @Override
  public User getById(String id) {
    try {
      return querySingle("SELECT * FROM users WHERE id = ?", Integer.parseInt(id));
    } catch (Exception e) {
      LOGGER.warn("getById failed for id={}", id, e);
      return null;
    }
  }

  @Override
  public List<User> getAllUsers() {
    return queryList("select * from users");
  }

  @Override
  public List<User> searchUsers(String keyword) {
    String sql =
        "select * from users where (lower(username) like ? or lower(fullname) like ?) and isactive = true and lower(role) != 'admin' limit 20";
    String kw = "%" + keyword.toLowerCase().trim() + "%";
    List<User> users = queryList(sql, kw, kw);
    users.forEach(u -> u.setPassword(""));
    return users;
  }

  @Override
  public String updateUserProfile(int userId, String fullName, String email, String phone) {
    String fn = normalize(fullName), em = normalize(email), ph = normalize(phone);
    if (em.isEmpty()) return "invalid_email";
    try (Connection conn = getConn()) {
      if (emailTakenByOtherUser(conn, userId, em)) return "duplicate_email";
      if (!ph.isEmpty() && phoneTakenByOtherUser(conn, userId, ph)) return "duplicate_phone";
      String sql = "UPDATE users SET fullname = ?, email = ?, phonenumber = ? WHERE id = ?";
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, fn); ps.setString(2, em); ps.setString(3, ph); ps.setInt(4, userId);
        ps.executeUpdate();
      }
      return null;
    } catch (SQLIntegrityConstraintViolationException e) { return "duplicate_email"; }
    catch (Exception e) { return "update_failed"; }
  }

  @Override
  public void updateAvatar(String username, String avatarUrl) throws Exception {
    String sql = "UPDATE users SET avatar_url = ? WHERE username = ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, avatarUrl); ps.setString(2, username); ps.executeUpdate();
    }
  }

  @Override
  public boolean updateBalance(int userId, double newBalance) {
    return executeUpdate("UPDATE users SET balance = ? WHERE id = ?", newBalance, userId);
  }

  public boolean atomicDeductBalance(int userId, double amount) {
    return executeUpdate("UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?", amount, userId, amount);
  }

  public boolean atomicCreditBalance(int userId, double amount) {
    return executeUpdate("UPDATE users SET balance = balance + ? WHERE id = ?", amount, userId);
  }

  @Override
  public boolean setUserLocked(String username, boolean lockStatus) {
    return executeUpdate("UPDATE users SET islocked = ? WHERE username = ?", lockStatus, username);
  }

  @Override
  public boolean setUserRole(String username, String role) {
    return executeUpdate("UPDATE users SET role = ? WHERE username = ?", role, username);
  }

  @Override
  public boolean addBidderMetrics(int userId, double amount) {
    return executeUpdate(
        "UPDATE users SET moneyspent = moneyspent + ?, itemsbought = itemsbought + 1 WHERE id = ?", amount, userId);
  }

  @Override
  public boolean addSellerMetrics(int userId, double amount) {
    return executeUpdate(
        "UPDATE users SET moneyreceived = moneyreceived + ?, itemssold = itemssold + 1 WHERE id = ?", amount, userId);
  }

  public User getByUsername(String username) {
    return querySingle("SELECT * FROM users WHERE username = ? LIMIT 1", username);
  }

  public boolean creditBalanceTx(int userId, double amount, Connection conn) throws SQLException {
    String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setDouble(1, amount); ps.setInt(2, userId); return ps.executeUpdate() > 0;
    }
  }

  private boolean existsDuplicateUser(Connection conn, String username, String email) throws SQLException {
    String sql = "select 1 from users where lower(trim(username)) = lower(trim(?)) or lower(trim(email)) = lower(trim(?)) limit 1";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, username); ps.setString(2, email);
      try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
    }
  }

  private String normalize(String value) { return value == null ? "" : value.trim(); }

  private boolean emailTakenByOtherUser(Connection conn, int userId, String email) throws SQLException {
    String sql = "select 1 from users where id <> ? and lower(trim(email)) = lower(trim(?)) limit 1";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, userId); ps.setString(2, email);
      try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
    }
  }

  private boolean phoneTakenByOtherUser(Connection conn, int userId, String phone) throws SQLException {
    String sql = "select 1 from users where id <> ? and trim(coalesce(phonenumber,'')) <> '' and lower(trim(phonenumber)) = lower(trim(?)) limit 1";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, userId); ps.setString(2, phone);
      try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
    }
  }
}
