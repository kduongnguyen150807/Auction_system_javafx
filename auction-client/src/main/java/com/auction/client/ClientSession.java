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
      fullName = safe(user.getfullname());
      if (fullName.isBlank()) fullName = safe(user.getusername());
      email = safe(user.getemail());
      phone = safe(user.getphonenumber());
      if (activeRole == null) {
        activeRole = user.getrole() == null ? UserRole.BIDDER : user.getrole();
      }
    }
  }

  public static User getCurrentUser() {
    User ans = currentUser;
    return ans;
  }

  public static String getUsername() {
    String ans = currentUser == null ? "" : safe(currentUser.getusername());
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
    res.put("userid", String.valueOf(currentUser.getid()));
    res.put("fullname", newFullName);
    res.put("email", newEmail);
    res.put("phone", newPhone);
    com.auction.shared.Request req = new com.auction.shared.Request(com.auction.shared.Request.updateprofile, res);
    com.auction.shared.Response ans = com.auction.client.network.NetworkClient.getinstance().sendrequestandwait(req);
    if (ans != null && com.auction.shared.Response.ok.equals(ans.getstatus())) {
      fullName = safe(newFullName);
      email = safe(newEmail);
      phone = safe(newPhone);
      currentUser.setfullname(fullName);
      currentUser.setemail(email);
      currentUser.setphonenumber(phone);
      return null;
    }
    String ans2 = ans != null ? ans.getmessage() : "fail";
    return ans2;
  }

  public static void updateavatar(String ans) {
    if (currentUser != null) {
      currentUser.setavatarurl(ans);
      com.auction.shared.Request req = new com.auction.shared.Request(com.auction.shared.Request.updateavatar, currentUser.getusername() + " " + ans);
      com.auction.client.network.NetworkClient.getinstance().sendrequestandwait(req);
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