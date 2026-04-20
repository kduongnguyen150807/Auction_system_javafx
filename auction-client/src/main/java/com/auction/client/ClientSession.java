package com.auction.client;

import com.auction.client.network.NetworkClient;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import com.auction.shared.UserRole;
import java.util.HashMap;
import java.util.Map;

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
    return currentUser;
  }

  public static String getUsername() {
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

  public static String updateProfile(String newFullName, String newEmail, String newPhone) {
    if (currentUser == null) return "not_logged_in";

    Map<String, String> data = new HashMap<>();
    data.put("userid", String.valueOf(currentUser.getId()));
    data.put("fullname", newFullName);
    data.put("email", newEmail);
    data.put("phone", newPhone);

    Request request = new Request(Request.UPDATE_PROFILE, data);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);

    if (response != null && Response.OK.equals(response.getStatus())) {
      fullName = safe(newFullName);
      email = safe(newEmail);
      phone = safe(newPhone);
      currentUser.setFullName(fullName);
      currentUser.setEmail(email);
      currentUser.setPhoneNumber(phone);
      return null;
    }
    return response != null ? response.getMessage() : "fail";
  }

  public static void updateAvatar(String avatarUrl) {
    if (currentUser != null) {
      currentUser.setAvatarUrl(avatarUrl);
      Request request = new Request(Request.UPDATE_AVATAR, currentUser.getUsername() + " " + avatarUrl);
      NetworkClient.getInstance().sendRequestAndWait(request);
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
    return value == null ? "" : value;
  }
}
