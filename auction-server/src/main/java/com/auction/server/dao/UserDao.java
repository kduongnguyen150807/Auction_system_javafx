package com.auction.server.dao;

import com.auction.shared.user.User;
import com.auction.shared.user.UserFactory;

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
  public User login(String username, String password) {
    String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND isactive = true";
    List<User> results =  query(sql, List.of(username, password), this::mapUser);
    return results.isEmpty() ? null : results.getFirst();
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
      user.setVersion(resultSet.getInt("version"));
      user.setUsername(resultSet.getString("username"));
      user.setFullName(resultSet.getString("fullname"));
      user.setEmail(resultSet.getString("email"));
      user.setPhoneNumber(resultSet.getString("phonenumber"));
      user.setBalance(resultSet.getDouble("balance"));
      user.setAvatarUrl(resultSet.getString("avatar_url"));
      user.setAvgRating(resultSet.getDouble("avgrating"));
      user.setTotalRatings(resultSet.getInt("totalratings"));
      user.setActive(resultSet.getBoolean("isactive"));
      user.setLocked(resultSet.getBoolean("islocked"));
      user.setMoneySpent(resultSet.getDouble("moneyspent"));
      user.setItemsBought(resultSet.getInt("itemsbought"));
      user.setMoneyReceived(resultSet.getDouble("moneyreceived"));
      user.setItemsSold(resultSet.getInt("itemssold"));
      return user;
    }catch (SQLException e){
      System.out.println("error setting attribute");
    }
    return null;
  }
}
