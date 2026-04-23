package com.auction.server.service;

import com.auction.server.dao.UserDao;
import com.auction.shared.*;

public class UserService {
  private UserDao userDao;

  public UserService() {
    this.userDao = new UserDao();
  }

  public User login(String u, String p) {
    User ans = this.userDao.login(u, p);
    return ans;
  }

  public boolean signup(User u) {
    boolean ans = this.userDao.signup(u);
    return ans;
  }

  /**
   * @return null nếu thành công; mã lỗi nếu không (vd. duplicate_email).
   */
  public String updateProfile(int userId, String fullName, String email, String phone) {
    return this.userDao.updateUserProfile(userId, fullName, email, phone);
  }

  public void updateAvatar(String username, String ans) throws Exception {
    this.userDao.updateAvatar(username, ans);
  }

  public java.util.List<User> getAllUsers() {
    java.util.List<User> ans = this.userDao.getAllUsers();
    return ans;
  }

  public boolean setUserLocked(String username, boolean lockStatus) {
    boolean ans = this.userDao.setUserLocked(username, lockStatus);
    return ans;
  }

  public boolean setUserRole(String username, String role) {
    boolean ans = this.userDao.setUserRole(username, role);
    return ans;
  }

  public java.util.List<User> searchUsers(String keyword) {
    return this.userDao.searchUsers(keyword);
  }

  public User getUserById(int id) {
    return this.userDao.getById(String.valueOf(id));
  }
}
