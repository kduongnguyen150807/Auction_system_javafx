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

    User user =
            role.equalsIgnoreCase("ADMIN")
                    ? new Admin()
                    : role.equalsIgnoreCase("SELLER")
                    ? new Seller()
                    : new Bidder();

    user.setId(rs.getInt("id"));
    user.setVersion(rs.getInt("version"));

    user.setUsername(rs.getString("username"));
    user.setFullName(rs.getString("fullname"));

    user.setPassword(rs.getString("password"));
    user.setEmail(rs.getString("email"));

    user.setAge(rs.getString("age"));
    user.setPhoneNumber(rs.getString("phonenumber"));

    user.setBalance(rs.getDouble("balance"));

    user.setMoneySpent(rs.getDouble("moneyspent"));
    user.setItemsBought(rs.getInt("itemsbought"));

    user.setMoneyReceived(rs.getDouble("moneyreceived"));
    user.setItemsSold(rs.getInt("itemssold"));

    user.setAvgRating(rs.getDouble("avgrating"));
    user.setTotalRatings(rs.getInt("totalratings"));

    user.setActive(rs.getBoolean("isactive"));
    user.setLocked(rs.getBoolean("islocked"));

    user.setAvatarUrl(rs.getString("avatar_url"));

    return user;
  }

  @Override
  public User login(String username, String password) {

    return querySingle(
            "SELECT * FROM users WHERE username = ? AND password = ? AND isactive = true AND islocked = false",
            username,
            password);
  }

  @Override
  public boolean signup(User user) {

    try (Connection conn = getConn()) {

      String username = normalize(user.getUsername());
      String email = normalize(user.getEmail());

      if (existsDuplicateUser(conn, username, email)) {
        return false;
      }

      String sql =
              """
              INSERT INTO users(
                  username,
                  fullname,
                  password,
                  email,
                  age,
                  phonenumber,
                  role,
                  isactive,
                  islocked,
                  balance,
                  moneyspent,
                  itemsbought,
                  moneyreceived,
                  itemssold,
                  avgrating,
                  totalratings
              )
              VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
              """;

      try (PreparedStatement ps = conn.prepareStatement(sql)) {

        String profileName = normalize(user.getFullName());

        if (profileName.isBlank()) {
          profileName = username;
        }

        ps.setString(1, username);
        ps.setString(2, profileName);

        ps.setString(3, user.getPassword());
        ps.setString(4, email);

        ps.setString(5, user.getAge());
        ps.setString(6, user.getPhoneNumber());

        ps.setString(7, user.getRole().name());

        ps.setBoolean(8, true);
        ps.setBoolean(9, false);

        ps.setDouble(10, 0.0);

        ps.setDouble(11, 0.0);
        ps.setInt(12, 0);

        ps.setDouble(13, 0.0);
        ps.setInt(14, 0);

        ps.setDouble(15, 0.0);
        ps.setInt(16, 0);

        return ps.executeUpdate() > 0;
      }

    } catch (SQLIntegrityConstraintViolationException e) {

      return false;

    } catch (Exception e) {

      LOGGER.warn("Signup failed", e);
      return false;
    }
  }

  @Override
  public User getById(String id) {

    try {

      return querySingle(
              "SELECT * FROM users WHERE id = ?",
              Integer.parseInt(id));

    } catch (Exception e) {

      LOGGER.warn("getById failed for id={}", id, e);
      return null;
    }
  }

  @Override
  public List<User> getAllUsers() {
    return queryList("SELECT * FROM users");
  }

  @Override
  public List<User> searchUsers(String keyword) {

    String sql =
            """
            SELECT *
            FROM users
            WHERE
                (
                    LOWER(username) LIKE ?
                    OR LOWER(fullname) LIKE ?
                )
                AND isactive = true
                AND LOWER(role) != 'admin'
            LIMIT 20
            """;

    String kw = "%" + keyword.toLowerCase().trim() + "%";

    List<User> users = queryList(sql, kw, kw);

    users.forEach(u -> u.setPassword(""));

    return users;
  }

  @Override
  public String updateUserProfile(
          int userId,
          String fullName,
          String email,
          String phone) {

    String fn = normalize(fullName);
    String em = normalize(email);
    String ph = normalize(phone);

    if (em.isEmpty()) {
      return "invalid_email";
    }

    try (Connection conn = getConn()) {

      if (emailTakenByOtherUser(conn, userId, em)) {
        return "duplicate_email";
      }

      if (!ph.isEmpty() && phoneTakenByOtherUser(conn, userId, ph)) {
        return "duplicate_phone";
      }

      String sql =
              """
              UPDATE users
              SET
                  fullname = ?,
                  email = ?,
                  phonenumber = ?
              WHERE id = ?
              """;

      try (PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, fn);
        ps.setString(2, em);
        ps.setString(3, ph);
        ps.setInt(4, userId);

        ps.executeUpdate();
      }

      return null;

    } catch (SQLIntegrityConstraintViolationException e) {

      return "duplicate_email";

    } catch (Exception e) {

      return "update_failed";
    }
  }

  @Override
  public void updateAvatar(String username, String avatarUrl) throws Exception {

    String sql =
            "UPDATE users SET avatar_url = ? WHERE username = ?";

    try (
            Connection conn = getConn();
            PreparedStatement ps = conn.prepareStatement(sql)
    ) {

      ps.setString(1, avatarUrl);
      ps.setString(2, username);

      ps.executeUpdate();
    }
  }

  public boolean atomicDeductBalance(int userId, double amount) {

    if (amount <= 0) {
      return false;
    }

    return executeUpdate(
            "UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?",
            amount,
            userId,
            amount);
  }

  public boolean atomicCreditBalance(int userId, double amount) {

    if (amount <= 0) {
      return false;
    }

    return executeUpdate(
            "UPDATE users SET balance = balance + ? WHERE id = ?",
            amount,
            userId);
  }

  @Override
  public boolean setUserLocked(String username, boolean lockStatus) {

    return executeUpdate(
            "UPDATE users SET islocked = ? WHERE username = ?",
            lockStatus,
            username);
  }

  @Override
  public boolean setUserRole(String username, String role) {

    return executeUpdate(
            "UPDATE users SET role = ? WHERE username = ?",
            role,
            username);
  }

  public boolean updatePasswordByEmail(
          String email,
          String newHashedPassword) {

    return executeUpdate(
            "UPDATE users SET password = ? WHERE email = ?",
            newHashedPassword,
            email);
  }

  public boolean isEmailExists(String email) {

    User u =
            querySingle(
                    "SELECT * FROM users WHERE email = ? LIMIT 1",
                    email);

    return u != null;
  }

  @Override
  public boolean addBidderMetrics(int userId, double amount) {

    return executeUpdate(
            """
            UPDATE users
            SET
                moneyspent = moneyspent + ?,
                itemsbought = itemsbought + 1
            WHERE id = ?
            """,
            amount,
            userId);
  }

  @Override
  public boolean addSellerMetrics(int userId, double amount) {

    return executeUpdate(
            """
            UPDATE users
            SET
                moneyreceived = moneyreceived + ?,
                itemssold = itemssold + 1
            WHERE id = ?
            """,
            amount,
            userId);
  }

  public User getByUsername(String username) {

    return querySingle(
            "SELECT * FROM users WHERE username = ? LIMIT 1",
            username);
  }

  // Xử lí lỗi nếu đang bid chưa kịp trừ tiền thì server sập

  public boolean deductBalanceTx(
          int userId,
          double amount,
          Connection conn) throws SQLException {

    if (amount <= 0) {
      return false;
    }

    String sql =
            """
            UPDATE users
            SET balance = balance - ?
            WHERE id = ?
            AND balance >= ?
            """;

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setDouble(1, amount);
      ps.setInt(2, userId);
      ps.setDouble(3, amount);

      return ps.executeUpdate() > 0;
    }
  }

  public boolean creditBalanceTx(
          int userId,
          double amount,
          Connection conn) throws SQLException {

    if (amount <= 0) {
      return false;
    }

    String sql =
            "UPDATE users SET balance = balance + ? WHERE id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setDouble(1, amount);
      ps.setInt(2, userId);

      return ps.executeUpdate() > 0;
    }
  }

  private boolean existsDuplicateUser(
          Connection conn,
          String username,
          String email) throws SQLException {

    String sql =
            """
            SELECT 1
            FROM users
            WHERE
                LOWER(TRIM(username)) = LOWER(TRIM(?))
                OR LOWER(TRIM(email)) = LOWER(TRIM(?))
            LIMIT 1
            """;

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, username);
      ps.setString(2, email);

      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim();
  }

  private boolean emailTakenByOtherUser(
          Connection conn,
          int userId,
          String email) throws SQLException {

    String sql =
            """
            SELECT 1
            FROM users
            WHERE
                id <> ?
                AND LOWER(TRIM(email)) = LOWER(TRIM(?))
            LIMIT 1
            """;

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, userId);
      ps.setString(2, email);

      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  private boolean phoneTakenByOtherUser(
          Connection conn,
          int userId,
          String phone) throws SQLException {

    String sql =
            """
            SELECT 1
            FROM users
            WHERE
                id <> ?
                AND TRIM(COALESCE(phonenumber,'')) <> ''
                AND LOWER(TRIM(phonenumber)) = LOWER(TRIM(?))
            LIMIT 1
            """;

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, userId);
      ps.setString(2, phone);

      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }
}