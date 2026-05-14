package com.auction.server.dao;

import com.auction.shared.user.User;
import com.auction.shared.user.UserFactory;
import com.auction.shared.user.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object (DAO) cho thực thể {@link User}.
 *
 * <p>Chịu trách nhiệm:
 * <ul>
 *   <li>Thực hiện truy vấn liên quan đến bảng users trong database.</li>
 *   <li>Chuyển đổi {@link ResultSet} thành object {@link User}.</li>
 * </ul>
 *
 * <p>Class này kế thừa {@link BaseDao} để sử dụng các phương thức query chung.</p>
 */
public class UserDao extends BaseDao{
  private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);
  /**
   * Thực hiện đăng nhập bằng username và password.
   *
   * <p>Chỉ trả về user nếu:
   * <ul>
   *   <li>Username và password khớp</li>
   *   <li>Tài khoản đang active</li>
   * </ul>
   *
   * @param username tên đăng nhập
   * @param password mật khẩu
   * @return {@link User} nếu đăng nhập thành công, ngược lại trả về null
   */
  public User login(String username, String password, Connection connection) {
    String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'ACTIVE'";
    List<User> results =  query(connection, sql, List.of(username, password), this::mapUser);
    return results.isEmpty() ? null : results.getFirst();
  }

  public boolean register(String username, String password, String email, int age, Connection connection) {
    String sql = "INSERT INTO users (username, password, email, age) VALUES (?, ?, ?, ?)";
    return update(connection, sql, List.of(username, password, email, age));
  }

  public boolean updateBalance(int userId, double amount, Connection connection) {
    String sql = "UPDATE users SET balance = balance + ? WHERE id = ? AND balance + ? > 0";
    return update(connection, sql, List.of(amount, userId, amount));
  }

  public User findForUpdate(int userId, Connection connection) {
    String sql = "SELECT * FROM users WHERE id = ? FOR UPDATE";
    List<User> list = query(connection, sql, List.of(userId), this::mapUser);
    return list.isEmpty() ? null : list.getFirst();
  }

  public User findById(int id, Connection connection) {
    String sql = "SELECT * FROM users WHERE id = ?";
    List<User> list = query(connection, sql, List.of(id), this::mapUser);
    return list.isEmpty() ? null : list.get(0);
  }

  /**
   * Mapping một dòng dữ liệu từ {@link ResultSet} sang object {@link User}.
   *
   * <p>Sử dụng {@link UserFactory} để tạo đúng loại user dựa trên role.</p>
   *
   * @param resultSet dữ liệu trả về từ database
   * @return đối tượng {@link User} đã được mapping, hoặc null nếu xảy ra lỗi
   */
  public User mapUser(ResultSet resultSet) {
    try {
      String role = resultSet.getString("role");
      User user = UserFactory.create(role);

      user.setId(resultSet.getInt("id"));
      user.setUsername(resultSet.getString("username"));
      user.setFullName(resultSet.getString("fullname"));
      user.setEmail(resultSet.getString("email"));
      user.setPhoneNumber(resultSet.getString("phonenumber"));
      user.setBalance(resultSet.getDouble("balance"));
      user.setActive(resultSet.getBoolean("isactive"));
      user.setAge(resultSet.getInt("age"));
      String statusStr = resultSet.getString("status");
      if (statusStr != null) {
        try {
          user.setStatus(UserStatus.valueOf(statusStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
          LOGGER.warn("Unknown status '{}' for user {}, defaulting to ACTIVE", statusStr, user.getFullName());
          user.setStatus(UserStatus.ACTIVE);
        }
      }
      return user;
    }catch (SQLException e){
      LOGGER.info(e.getMessage());
    }
    return null;
  }
}
