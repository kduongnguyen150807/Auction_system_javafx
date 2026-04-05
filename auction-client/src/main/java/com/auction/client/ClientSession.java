package com.auction.client;

import com.auction.shared.User;
import com.auction.shared.UserRole;

public final class ClientSession {
  private static User currentUser;
  private static String fullName = "";
  private static String email = "";
  private static String phone = "";
  private static UserRole activeRole;

  private ClientSession() {}

  public static void setCurrentUser(User user) {
    currentUser = user;
    if (user != null) {
      fullName = safe(user.getFullName());
      if (fullName.isBlank()) fullName = safe(user.getUsername());
      email = safe(user.getEmail());
      phone = safe(user.getPhoneNumber());
      if (activeRole == null) {
        activeRole = user.getRole() == null ? UserRole.BIDDER : user.getRole();
      }
    }
  }

  public static User getCurrentUser() {
    User ans = currentUser;
    return ans;
  }

  public static String getUsername() {
    String ans = currentUser == null ? "" : safe(currentUser.getUsername());
    return ans;
  }

  public static String getFullName() {
    String ans = fullName;
    return ans;
  }

  public static String getEmail() {
    String ans = email;
    return ans;
  }

  public static String getPhone() {
    String ans = phone;
    return ans;
  }

  public static UserRole getActiveRole() {
    UserRole ans = activeRole;
    return ans;
  }

  public static String updateProfile(String newFullName, String newEmail, String newPhone) {
    if (currentUser == null) return "not_logged_in";
    java.util.Map<String, String> res = new java.util.HashMap<>();
    res.put("userid", String.valueOf(currentUser.getId()));
    res.put("fullname", newFullName);
    res.put("email", newEmail);
    res.put("phone", newPhone);
    com.auction.shared.Request req =
        new com.auction.shared.Request(com.auction.shared.Request.UPDATE_PROFILE, res);
    com.auction.shared.Response ans =
        com.auction.client.network.NetworkClient.getInstance().sendRequestAndWait(req);
    if (ans != null && com.auction.shared.Response.OK.equals(ans.getStatus())) {
      fullName = safe(newFullName);
      email = safe(newEmail);
      phone = safe(newPhone);
      currentUser.setFullName(fullName);
      currentUser.setEmail(email);
      currentUser.setPhoneNumber(phone);
      return null;
    }
    String ans2 = ans != null ? ans.getMessage() : "fail";
    return ans2;
  }

  public static void updateAvatar(String ans) {
    if (currentUser != null) {
      currentUser.setAvatarUrl(ans);
      com.auction.shared.Request req =
          new com.auction.shared.Request(
              com.auction.shared.Request.UPDATE_AVATAR, currentUser.getUsername() + " " + ans);
      com.auction.client.network.NetworkClient.getInstance().sendRequestAndWait(req);
    }
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

  private static String safe(String value) {
    String ans = value == null ? "" : value;
    return ans;
  }
}
