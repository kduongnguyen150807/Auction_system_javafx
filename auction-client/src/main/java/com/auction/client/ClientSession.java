package com.auction.client;

import com.auction.shared.User;
import com.auction.shared.UserRole;

public final class ClientSession {
  private static User currentUser;
  private static String fullName = "";
  private static String email = "";
  private static String phone = "";
  private static UserRole activeRole = UserRole.BIDDER;

  private ClientSession() {}

  public static void setCurrentUser(User user) {
    currentUser = user;
    if (user != null) {
      fullName = safe(user.getusername());
      email = safe(user.getemail());
      phone = safe(user.getphonenumber());
      activeRole = user.getrole() == null ? UserRole.BIDDER : user.getrole();
    }
  }

  public static User getCurrentUser() {
    return currentUser;
  }

  public static String getUsername() {
    return currentUser == null ? "" : safe(currentUser.getusername());
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

  public static void updateProfile(String newFullName, String newEmail, String newPhone) {
    if (currentUser == null) return;
    java.util.Map<String, String> data = new java.util.HashMap<>();
    data.put("userid", String.valueOf(currentUser.getid()));
    data.put("fullname", newFullName);
    data.put("email", newEmail);
    data.put("phone", newPhone);
    com.auction.shared.Request req = new com.auction.shared.Request(com.auction.shared.Request.updateprofile, data);
    com.auction.shared.Response ans =
        com.auction.client.network.NetworkClient.getinstance().sendrequestandwait(req);
    if (ans != null && com.auction.shared.Response.ok.equals(ans.getstatus())) {
      fullName = safe(newFullName);
      email = safe(newEmail);
      phone = safe(newPhone);
      currentUser.setusername(fullName);
      currentUser.setemail(email);
      currentUser.setphonenumber(phone);
    }
  }

  public static void updateavatar(String ans) {
    if (currentUser != null) {
      currentUser.setavatarurl(ans);
      com.auction.shared.Request req = new com.auction.shared.Request(com.auction.shared.Request.updateavatar, currentUser.getusername() + " " + ans);
      com.auction.client.network.NetworkClient.getinstance().sendrequestandwait(req);
    }
  }

  public static void toggleRole() {
    activeRole = activeRole == UserRole.SELLER ? UserRole.BIDDER : UserRole.SELLER;
  }

  public static void clear() {
    currentUser = null;
    fullName = "";
    email = "";
    phone = "";
    activeRole = UserRole.BIDDER;
  }

  private static String safe(String value) {
    return value == null ? "" : value;
  }
}
