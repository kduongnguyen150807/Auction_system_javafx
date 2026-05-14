package com.auction.server.dao.user;

import com.auction.shared.User;
import java.util.List;

public interface UserRepository {
  User login(String username, String password);
  boolean signup(User user);
  User getById(String id);
  List<User> getAllUsers();
  List<User> searchUsers(String keyword);
  String updateUserProfile(int userId, String fullName, String email, String phone);
  void updateAvatar(String username, String avatarUrl) throws Exception;
  boolean setUserLocked(String username, boolean lockStatus);
  boolean setUserRole(String username, String role);
  boolean addBidderMetrics(int userId, double amount);
  boolean addSellerMetrics(int userId, double amount);
}
