package com.auction.server.dao;

import com.auction.shared.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao implements UserRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

  private Connection getConn() { return DatabaseConnection.getInstance().getConnection(); }

  @Override
  public User login(String username, String password) {
    String sql = "select * from users where username = ? and password = ? and isactive = true and islocked = false";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, username); ps.setString(2, password);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) return mapResultSetToUser(rs);
    } catch (Exception e) { LOGGER.warn("Login query failed", e); }
    return null;
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
    String sql = "SELECT * FROM users WHERE id = ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, Integer.parseInt(id));
      ResultSet rs = ps.executeQuery();
      if (rs.next()) return mapResultSetToUser(rs);
    } catch (Exception e) { LOGGER.warn("getById failed for id={}", id, e); }
    return null;
  }

  @Override
  public List<User> getAllUsers() {
    List<User> users = new ArrayList<>();
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement("select * from users")) {
      ResultSet rs = ps.executeQuery();
      while (rs.next()) users.add(mapResultSetToUser(rs));
    } catch (Exception e) { LOGGER.warn("getAllUsers failed", e); }
    return users;
  }

  @Override
  public List<User> searchUsers(String keyword) {
    List<User> users = new ArrayList<>();
    String sql = "select * from users where (lower(username) like ? or lower(fullname) like ?) and isactive = true and lower(role) != 'admin' limit 20";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      String kw = "%" + keyword.toLowerCase().trim() + "%";
      ps.setString(1, kw); ps.setString(2, kw);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) { User u = mapResultSetToUser(rs); u.setPassword(""); users.add(u); }
    } catch (Exception e) { LOGGER.warn("searchUsers failed", e); }
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
    String sql = "UPDATE users SET balance = ? WHERE id = ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setDouble(1, newBalance); ps.setInt(2, userId); return ps.executeUpdate() > 0;
    } catch (Exception e) { LOGGER.warn("updateBalance failed", e); return false; }
  }

  public boolean atomicDeductBalance(int userId, double amount) {
    String sql = "UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setDouble(1, amount); ps.setInt(2, userId); ps.setDouble(3, amount); return ps.executeUpdate() > 0;
    } catch (Exception e) { LOGGER.warn("atomicDeductBalance failed", e); return false; }
  }

  public boolean atomicCreditBalance(int userId, double amount) {
    String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setDouble(1, amount); ps.setInt(2, userId); return ps.executeUpdate() > 0;
    } catch (Exception e) { LOGGER.warn("atomicCreditBalance failed", e); return false; }
  }

  @Override
  public boolean setUserLocked(String username, boolean lockStatus) {
    String sql = "UPDATE users SET islocked = ? WHERE username = ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setBoolean(1, lockStatus); ps.setString(2, username); return ps.executeUpdate() > 0;
    } catch (Exception e) { LOGGER.warn("setUserLocked failed", e); return false; }
  }

  @Override
  public boolean setUserRole(String username, String role) {
    String sql = "UPDATE users SET role = ? WHERE username = ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, role); ps.setString(2, username); return ps.executeUpdate() > 0;
    } catch (Exception e) { LOGGER.warn("setUserRole failed", e); return false; }
  }

  @Override
  public boolean addBidderMetrics(int userId, double amount) {
    String sql = "UPDATE users SET moneyspent = moneyspent + ?, itemsbought = itemsbought + 1 WHERE id = ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setDouble(1, amount); ps.setInt(2, userId); return ps.executeUpdate() > 0;
    } catch (Exception e) { LOGGER.warn("addBidderMetrics failed", e); return false; }
  }

  @Override
  public boolean addSellerMetrics(int userId, double amount) {
    String sql = "UPDATE users SET moneyreceived = moneyreceived + ?, itemssold = itemssold + 1 WHERE id = ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setDouble(1, amount); ps.setInt(2, userId); return ps.executeUpdate() > 0;
    } catch (Exception e) { LOGGER.warn("addSellerMetrics failed", e); return false; }
  }

  public User getByUsername(String username) {
    String sql = "SELECT * FROM users WHERE username = ? LIMIT 1";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapResultSetToUser(rs); }
    } catch (Exception e) { LOGGER.warn("getByUsername failed for {}", username, e); }
    return null;
  }

  public boolean creditBalanceTx(int userId, double amount, Connection conn) throws SQLException {
    String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setDouble(1, amount); ps.setInt(2, userId); return ps.executeUpdate() > 0;
    }
  }

  private User mapResultSetToUser(ResultSet rs) throws SQLException {
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
