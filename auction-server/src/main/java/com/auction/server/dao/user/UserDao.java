package com.auction.server.dao.user;

import com.auction.server.dao.platform.BaseDao;
import com.auction.shared.Admin;
import com.auction.shared.Bidder;
import com.auction.shared.Seller;
import com.auction.shared.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao extends BaseDao<User> implements UserRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

  private static final String LOGIN_SQL =
          "SELECT * FROM users "
                  + "WHERE username = ? AND password = ? AND isactive = true AND islocked = false";

  private static final String SIGNUP_SQL =
          "INSERT INTO users(username, fullname, password, email, age, phonenumber, role, isactive, islocked) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String GET_BY_ID_SQL =
          "SELECT * FROM users WHERE id = ?";

  private static final String GET_ALL_USERS_SQL =
          "SELECT * FROM users";

  private static final String SEARCH_USERS_SQL =
          "SELECT * FROM users "
                  + "WHERE (LOWER(username) LIKE ? OR LOWER(fullname) LIKE ?) "
                  + "AND isactive = true "
                  + "AND LOWER(role) != 'admin' "
                  + "LIMIT 20";

  private static final String UPDATE_PROFILE_SQL =
          "UPDATE users SET fullname = ?, email = ?, phonenumber = ? WHERE id = ?";

  private static final String UPDATE_AVATAR_SQL =
          "UPDATE users SET avatar_url = ? WHERE username = ?";

  private static final String ATOMIC_DEDUCT_BALANCE_SQL =
          "UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?";

  private static final String ATOMIC_CREDIT_BALANCE_SQL =
          "UPDATE users SET balance = balance + ? WHERE id = ?";

  private static final String SET_USER_LOCKED_SQL =
          "UPDATE users SET islocked = ? WHERE username = ?";

  private static final String SET_USER_ROLE_SQL =
          "UPDATE users SET role = ? WHERE username = ?";

  private static final String UPDATE_PASSWORD_BY_EMAIL_SQL =
          "UPDATE users SET password = ? WHERE email = ?";

  private static final String CHECK_EMAIL_EXISTS_SQL =
          "SELECT * FROM users WHERE email = ? LIMIT 1";

  private static final String ADD_BIDDER_METRICS_SQL =
          "UPDATE users SET moneyspent = moneyspent + ?, itemsbought = itemsbought + 1 WHERE id = ?";

  private static final String ADD_SELLER_METRICS_SQL =
          "UPDATE users SET moneyreceived = moneyreceived + ?, itemssold = itemssold + 1 WHERE id = ?";

  private static final String GET_BY_USERNAME_SQL =
          "SELECT * FROM users WHERE username = ? LIMIT 1";

  private static final String EXISTS_DUPLICATE_USER_SQL =
          "SELECT 1 FROM users "
                  + "WHERE LOWER(TRIM(username)) = LOWER(TRIM(?)) "
                  + "OR LOWER(TRIM(email)) = LOWER(TRIM(?)) "
                  + "LIMIT 1";

  private static final String EMAIL_TAKEN_BY_OTHER_USER_SQL =
          "SELECT 1 FROM users "
                  + "WHERE id <> ? AND LOWER(TRIM(email)) = LOWER(TRIM(?)) "
                  + "LIMIT 1";

  private static final String PHONE_TAKEN_BY_OTHER_USER_SQL =
          "SELECT 1 FROM users "
                  + "WHERE id <> ? "
                  + "AND TRIM(COALESCE(phonenumber, '')) <> '' "
                  + "AND LOWER(TRIM(phonenumber)) = LOWER(TRIM(?)) "
                  + "LIMIT 1";

  @Override
  protected User mapRow(ResultSet rs) throws SQLException {
    User user = createUserByRole(rs.getString("role"));

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
    user.setActive(rs.getBoolean("isactive"));
    user.setLocked(rs.getBoolean("islocked"));
    user.setAvatarUrl(rs.getString("avatar_url"));

    mapOptionalRatingFields(rs, user);

    return user;
  }

  private User createUserByRole(String role) {
    if (role == null) {
      return new Bidder();
    }

    if ("ADMIN".equalsIgnoreCase(role)) {
      return new Admin();
    }

    if ("SELLER".equalsIgnoreCase(role)) {
      return new Seller();
    }

    return new Bidder();
  }

  private void mapOptionalRatingFields(ResultSet rs, User user) {
    try {
      user.setAvgRating(rs.getDouble("avgrating"));
    } catch (SQLException e) {
      LOGGER.debug("Column avgrating is not available in this query", e);
    }

    try {
      user.setTotalRatings(rs.getInt("totalratings"));
    } catch (SQLException e) {
      LOGGER.debug("Column totalratings is not available in this query", e);
    }
  }

  @Override
  public User login(String username, String password) {
    return querySingle(LOGIN_SQL, username, password);
  }

  @Override
  public boolean signup(User user) {
    try (Connection conn = getConn()) {
      String username = normalize(user.getUsername());
      String email = normalize(user.getEmail());

      if (existsDuplicateUser(conn, username, email)) {
        return false;
      }

      try (PreparedStatement ps = conn.prepareStatement(SIGNUP_SQL)) {
        bindSignupParams(ps, user, username, email);
        return ps.executeUpdate() > 0;
      }
    } catch (SQLIntegrityConstraintViolationException e) {
      return false;
    } catch (Exception e) {
      LOGGER.warn("Signup failed", e);
      return false;
    }
  }

  private void bindSignupParams(
          PreparedStatement ps,
          User user,
          String username,
          String email)
          throws SQLException {

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
  }

  @Override
  public User getById(String id) {
    try {
      return querySingle(GET_BY_ID_SQL, Integer.parseInt(id));
    } catch (Exception e) {
      LOGGER.warn("getById failed for id={}", id, e);
      return null;
    }
  }

  @Override
  public List<User> getAllUsers() {
    return queryList(GET_ALL_USERS_SQL);
  }

  @Override
  public List<User> searchUsers(String keyword) {
    String safeKeyword = normalize(keyword).toLowerCase();
    String pattern = "%" + safeKeyword + "%";

    List<User> users = queryList(SEARCH_USERS_SQL, pattern, pattern);
    clearPasswords(users);

    return users;
  }

  private void clearPasswords(List<User> users) {
    for (User user : users) {
      user.setPassword("");
    }
  }

  @Override
  public String updateUserProfile(int userId, String fullName, String email, String phone) {
    String normalizedFullName = normalize(fullName);
    String normalizedEmail = normalize(email);
    String normalizedPhone = normalize(phone);

    if (normalizedEmail.isEmpty()) {
      return "invalid_email";
    }

    try (Connection conn = getConn()) {
      if (emailTakenByOtherUser(conn, userId, normalizedEmail)) {
        return "duplicate_email";
      }

      if (!normalizedPhone.isEmpty() && phoneTakenByOtherUser(conn, userId, normalizedPhone)) {
        return "duplicate_phone";
      }

      try (PreparedStatement ps = conn.prepareStatement(UPDATE_PROFILE_SQL)) {
        ps.setString(1, normalizedFullName);
        ps.setString(2, normalizedEmail);
        ps.setString(3, normalizedPhone);
        ps.setInt(4, userId);
        ps.executeUpdate();
      }

      return null;
    } catch (SQLIntegrityConstraintViolationException e) {
      return "duplicate_email";
    } catch (Exception e) {
      LOGGER.warn("updateUserProfile failed for userId={}", userId, e);
      return "update_failed";
    }
  }

  @Override
  public void updateAvatar(String username, String avatarUrl) throws Exception {
    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(UPDATE_AVATAR_SQL)) {

      ps.setString(1, avatarUrl);
      ps.setString(2, username);
      ps.executeUpdate();
    }
  }

  public boolean atomicDeductBalance(int userId, double amount) {
    if (!isValidAmount(amount)) {
      return false;
    }

    return executeUpdate(ATOMIC_DEDUCT_BALANCE_SQL, amount, userId, amount);
  }

  public boolean atomicCreditBalance(int userId, double amount) {
    if (!isValidAmount(amount)) {
      return false;
    }

    return executeUpdate(ATOMIC_CREDIT_BALANCE_SQL, amount, userId);
  }

  @Override
  public boolean setUserLocked(String username, boolean lockStatus) {
    return executeUpdate(SET_USER_LOCKED_SQL, lockStatus, username);
  }

  @Override
  public boolean setUserRole(String username, String role) {
    return executeUpdate(SET_USER_ROLE_SQL, role, username);
  }

  public boolean updatePasswordByEmail(String email, String newHashedPassword) {
    return executeUpdate(UPDATE_PASSWORD_BY_EMAIL_SQL, newHashedPassword, email);
  }

  public boolean isEmailExists(String email) {
    User user = querySingle(CHECK_EMAIL_EXISTS_SQL, email);
    return user != null;
  }

  @Override
  public boolean addBidderMetrics(int userId, double amount) {
    return executeUpdate(ADD_BIDDER_METRICS_SQL, amount, userId);
  }

  @Override
  public boolean addSellerMetrics(int userId, double amount) {
    return executeUpdate(ADD_SELLER_METRICS_SQL, amount, userId);
  }

  public User getByUsername(String username) {
    return querySingle(GET_BY_USERNAME_SQL, username);
  }

  public boolean deductBalanceTx(int userId, double amount, Connection conn)
          throws SQLException {

    if (!isValidAmount(amount)) {
      return false;
    }

    try (PreparedStatement ps = conn.prepareStatement(ATOMIC_DEDUCT_BALANCE_SQL)) {
      ps.setDouble(1, amount);
      ps.setInt(2, userId);
      ps.setDouble(3, amount);

      return ps.executeUpdate() > 0;
    }
  }

  public boolean creditBalanceTx(int userId, double amount, Connection conn)
          throws SQLException {

    if (!isValidAmount(amount)) {
      return false;
    }

    try (PreparedStatement ps = conn.prepareStatement(ATOMIC_CREDIT_BALANCE_SQL)) {
      ps.setDouble(1, amount);
      ps.setInt(2, userId);

      return ps.executeUpdate() > 0;
    }
  }

  private boolean existsDuplicateUser(Connection conn, String username, String email)
          throws SQLException {

    try (PreparedStatement ps = conn.prepareStatement(EXISTS_DUPLICATE_USER_SQL)) {
      ps.setString(1, username);
      ps.setString(2, email);

      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  private boolean emailTakenByOtherUser(Connection conn, int userId, String email)
          throws SQLException {

    try (PreparedStatement ps = conn.prepareStatement(EMAIL_TAKEN_BY_OTHER_USER_SQL)) {
      ps.setInt(1, userId);
      ps.setString(2, email);

      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  private boolean phoneTakenByOtherUser(Connection conn, int userId, String phone)
          throws SQLException {

    try (PreparedStatement ps = conn.prepareStatement(PHONE_TAKEN_BY_OTHER_USER_SQL)) {
      ps.setInt(1, userId);
      ps.setString(2, phone);

      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  private boolean isValidAmount(double amount) {
    return amount > 0;
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim();
  }
}