package com.auction.client;

import com.auction.shared.user.UserRole;
import com.auction.shared.user.User;

public class ClientSession {
  private static User currentUser;
  private static String fullName = "";
  private static String email = "";
  private static String phone = "";
  private static UserRole activeRole;

  private ClientSession() {
  }

  public static void setCurrentUser(User user) {
    currentUser = user;
    if (currentUser != null) {
      fullName = safe(currentUser.getFullName());
      if (fullName.isBlank()) {
        fullName = safe(user.getUsername());
      }
      email = safe(user.getEmail());
      phone = safe(user.getPhoneNumber());
      if (activeRole == null) {
        activeRole = user.getRole() == null ? UserRole.BIDDER : user.getRole();
      }
    }
  }

  private static String safe(String value) {
    return value == null ? "" : value;
  }

  public static void applyProfileUpdate(String newFullName, String newEmail, String newPhone) {
    if (currentUser == null) return;
    fullName = safe(newFullName);
    email = safe(newEmail);
    phone = safe(newPhone);
    currentUser.setFullName(fullName);
    currentUser.setEmail(email);
    currentUser.setPhoneNumber(phone);
  }

  public static User getCurrentUser() {
    return currentUser;
  }

  public static String getUserName() {
    return currentUser == null ? "" : safe(currentUser.getUsername());
  }

  public static String getFullName() {
    return fullName;
  }

  public static String getEmail() {
    return email;
  }

  public static String getPhone() {
    return phone;
  }

  public static UserRole getActiveRole() {
    return activeRole;
  }

  public static void toggleRole() {
    if (activeRole == UserRole.SELLER) activeRole = UserRole.BIDDER;
    else activeRole = UserRole.SELLER;
  }

  public static void clear() {
    currentUser = null;
    fullName = "";
    email = "";
    phone = "";
    activeRole = null;
  }
}
