package com.auction.server.dao.user;

import com.auction.server.dao.platform.BaseDao;
import com.auction.shared.*;

import java.sql.*;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao extends BaseDao<User> implements UserRepository {

  private static final Logger LOGGER =
          LoggerFactory.getLogger(UserDao.class);

  @Override
  protected User mapRow(ResultSet rs) throws SQLException {

    String role = rs.getString("role");

    User user =
            role.equalsIgnoreCase("ADMIN")
                    ? new Admin()
                    : new Bidder();

    user.setId(rs.getInt("id"));
    //user.setVersion(rs.getInt("version"));

    user.setUsername(rs.getString("username"));
    user.setFullName(rs.getString("fullname"));

    user.setPassword(rs.getString("password"));
    user.setEmail(rs.getString("email"));

    user.setAge(rs.getString("age"));
    user.setPhoneNumber(rs.getString("phone_number"));

    user.setBalance(rs.getDouble("balance"));

    user.setMoneySpent(rs.getDouble("money_spent"));
    user.setItemsBought(rs.getInt("items_bought"));

    user.setMoneyReceived(rs.getDouble("money_received"));
    user.setItemsSold(rs.getInt("items_sold"));

    user.setAvgRating(rs.getDouble("avg_rating"));
    user.setTotalRatings(rs.getInt("total_ratings"));

    user.setActive(rs.getBoolean("is_active"));
    user.setLocked(rs.getBoolean("is_locked"));

    user.setAvatarUrl(rs.getString("avatar_url"));

    user.setSessionToken(rs.getString("session_token"));

    return user;
  }

  @Override
  public User login(String username, String password) {

    User user = querySingle(
            """
            SELECT *
            FROM users
            WHERE username = ?
            AND password = ?
            AND is_active = true
            AND is_locked = false
            """,
            username,
            password);

    if (user == null) {
      return null;
    }
    if (user.getSessionToken() != null
            && !user.getSessionToken().isBlank()) {

      LOGGER.warn(
              "User already logged in elsewhere: {}",
              username);

      return null;
    }
    String sessionToken = UUID.randomUUID().toString();

    updateSessionToken(
            user.getId(),
            sessionToken);

    user.setSessionToken(sessionToken);

    return user;
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
                  phone_number,
                  role,
                  balance,
                  is_active,
                  is_locked,
                  avatar_url,
                  money_spent,
                  items_bought,
                  money_received,
                  items_sold,
                  avg_rating,
                  total_ratings,
                  session_token
              )
              VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
              """;

      try (PreparedStatement ps =
                   conn.prepareStatement(sql)) {

        String profileName =
                normalize(user.getFullName());

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

        ps.setDouble(8, 0.0);

        ps.setBoolean(9, true);
        ps.setBoolean(10, false);

        ps.setString(11, null);

        ps.setDouble(12, 0.0);
        ps.setInt(13, 0);

        ps.setDouble(14, 0.0);
        ps.setInt(15, 0);

        ps.setDouble(16, 0.0);
        ps.setInt(17, 0);

        ps.setString(18, null);

        return ps.executeUpdate() > 0;
      }

    } catch (Exception e) {

      LOGGER.error("Signup failed", e);
      return false;
    }
  }

  @Override
  public boolean updateSessionToken(
          int userId,
          String sessionToken) {

    return executeUpdate(
            """
            UPDATE users
            SET session_token = ?
            WHERE id = ?
            """,
            sessionToken,
            userId);
  }

  @Override
  public boolean clearSessionToken(int userId) {

    return executeUpdate(
            """
            UPDATE users
            SET session_token = NULL
            WHERE id = ?
            """,
            userId);
  }

  @Override
  public User getBySessionToken(
          String sessionToken) {

    return querySingle(
            """
            SELECT *
            FROM users
            WHERE session_token = ?
            """,
            sessionToken);
  }

  @Override
  public User getById(String id) {

    try {

      return querySingle(
              "SELECT * FROM users WHERE id = ?",
              Integer.parseInt(id));

    } catch (Exception e) {

      LOGGER.warn("getById failed", e);
      return null;
    }
  }

  @Override
  public User getByUsername(String username) {

    return querySingle(
            """
            SELECT *
            FROM users
            WHERE username = ?
            LIMIT 1
            """,
            username);
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
                AND is_active = true
            LIMIT 20
            """;

    String kw =
            "%" + keyword.toLowerCase().trim() + "%";

    List<User> users =
            queryList(sql, kw, kw);

    users.forEach(u -> u.setPassword(""));

    return users;
  }

  @Override
  public String updateUserProfile(
          int userId,
          String fullName,
          String email,
          String phone) {

    try {

      boolean ok =
              executeUpdate(
                      """
                      UPDATE users
                      SET
                          fullname = ?,
                          email = ?,
                          phone_number = ?
                      WHERE id = ?
                      """,
                      fullName,
                      email,
                      phone,
                      userId);

      return ok ? null : "update_failed";

    } catch (Exception e) {

      return "update_failed";
    }
  }

  @Override
  public void updateAvatar(
          String username,
          String avatarUrl) throws Exception {

    executeUpdate(
            """
            UPDATE users
            SET avatar_url = ?
            WHERE username = ?
            """,
            avatarUrl,
            username);
  }

  @Override
  public boolean setUserLocked(
          String username,
          boolean lockStatus) {

    return executeUpdate(
            """
            UPDATE users
            SET is_locked = ?
            WHERE username = ?
            """,
            lockStatus,
            username);
  }

  @Override
  public boolean setUserRole(
          String username,
          String role) {

    return executeUpdate(
            """
            UPDATE users
            SET role = ?
            WHERE username = ?
            """,
            role,
            username);
  }

  @Override
  public boolean addBidderMetrics(
          int userId,
          double amount) {

    return executeUpdate(
            """
            UPDATE users
            SET
                money_spent = money_spent + ?,
                items_bought = items_bought + 1
            WHERE id = ?
            """,
            amount,
            userId);
  }

  @Override
  public boolean addSellerMetrics(
          int userId,
          double amount) {

    return executeUpdate(
            """
            UPDATE users
            SET
                money_received = money_received + ?,
                items_sold = items_sold + 1
            WHERE id = ?
            """,
            amount,
            userId);
  }

  private boolean existsDuplicateUser(
          Connection conn,
          String username,
          String email)
          throws SQLException {

    String sql =
            """
            SELECT 1
            FROM users
            WHERE
                LOWER(username) = LOWER(?)
                OR LOWER(email) = LOWER(?)
            LIMIT 1
            """;

    try (PreparedStatement ps =
                 conn.prepareStatement(sql)) {

      ps.setString(1, username);
      ps.setString(2, email);

      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  private String normalize(String value) {

    return value == null
            ? ""
            : value.trim();
  }
}