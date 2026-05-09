package com.auction.server.service;

import com.auction.server.dao.UserDao;
import com.auction.shared.User;
import java.util.List;

public class UserService {
  private final UserDao userDao;

  public UserService() {
    this(new UserDao());
  }

  public UserService(UserDao userDao) {
    this.userDao = userDao;
  }

  public User login(String username, String password) {
    return userDao.login(username, password);
  }

  public boolean signup(User user) {
    return userDao.signup(user);
  }

  /**
   * @return null nếu thành công; mã lỗi nếu không, ví dụ duplicate_email.
   */
  public String updateProfile(int userId, String fullName, String email, String phone) {
    return userDao.updateUserProfile(userId, fullName, email, phone);
  }

  public void updateAvatar(String username, String avatarUrl) throws Exception {
    userDao.updateAvatar(username, avatarUrl);
  }

  public List<User> getAllUsers() {
    return userDao.getAllUsers();
  }

  public boolean setUserLocked(String username, boolean locked) {
    return userDao.setUserLocked(username, locked);
  }

  public boolean setUserRole(String username, String role) {
    return userDao.setUserRole(username, role);
  }

  public List<User> searchUsers(String keyword) {
    return userDao.searchUsers(keyword);
  }

  public User getUserById(int userId) {
    return userDao.getById(String.valueOf(userId));
  }
}