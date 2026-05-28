package com.auction.server.service.user;

import com.auction.server.dao.user.UserDao;
import com.auction.shared.User;
import java.util.List;

public class UserService {
  private UserDao userDao;

  public UserService() {
    this.userDao = new UserDao();
  }

  public User login(String username, String password) {
    return this.userDao.login(username, password);
  }

  public boolean signup(User user) {
    return this.userDao.signup(user);
  }

  public String updateProfile(int userId, String fullName, String email, String phone) {
    return this.userDao.updateUserProfile(userId, fullName, email, phone);
  }

  public void updateAvatar(String username, String avatarUrl) throws Exception {
    this.userDao.updateAvatar(username, avatarUrl);
  }

  public List<User> getAllUsers() {
    return this.userDao.getAllUsers();
  }

  public boolean setUserLocked(String username, boolean lockStatus) {
    return this.userDao.setUserLocked(username, lockStatus);
  }

  public boolean setUserRole(String username, String role) {
    return this.userDao.setUserRole(username, role);
  }

  public boolean isEmailExists(String email) {
    return this.userDao.isEmailExists(email);
  }

  public boolean updatePasswordByEmail(String email, String newPassword) {
    return this.userDao.updatePasswordByEmail(email, newPassword);
  }

  public boolean clearSessionToken(int userId) {
    return userDao.clearSessionToken(userId);
  }
}
