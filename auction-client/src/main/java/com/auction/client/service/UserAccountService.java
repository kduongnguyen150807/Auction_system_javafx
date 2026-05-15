package com.auction.client.service;

import com.auction.client.ClientSession;
import com.auction.client.network.NetworkClient;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserAccountService {

  public boolean deposit(int userId, double amount) {
    if (amount <= 0) return false;

    Map<String, String> data = new HashMap<>();
    data.put("userid", String.valueOf(userId));
    data.put("amount", String.valueOf(amount));
    Request request = new Request(Request.DEPOSIT, (Serializable) data);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    if (response != null && Response.OK.equals(response.getStatus())) {
      ClientSession.setCurrentUser((User) response.getPayload());
      return true;
    }
    return false;
  }

  /**
   * Sends a profile-update request to the server and, on success, syncs the
   * cached display fields in {@link ClientSession}. Returns {@code null} on
   * success or an error token on failure.
   */
  public String updateProfile(int userId, String fullName, String email, String phone) {
    Map<String, String> data = new HashMap<>();
    data.put("userid", String.valueOf(userId));
    data.put("fullname", fullName);
    data.put("email", email);
    data.put("phone", phone);
    Request request = new Request(Request.UPDATE_PROFILE, (Serializable) data);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    if (response != null && Response.OK.equals(response.getStatus())) {
      ClientSession.applyProfileUpdate(fullName, email, phone);
      return null;
    }
    return response != null ? response.getMessage() : "connection_error";
  }

  /**
   * Sends an avatar-update request and, on success, updates the local user
   * object held in {@link ClientSession}.
   */
  public boolean updateAvatar(String username, String avatarUrl) {
    Request request = new Request(Request.UPDATE_AVATAR, username + " " + avatarUrl);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    if (response != null && Response.OK.equals(response.getStatus())) {
      User current = ClientSession.getCurrentUser();
      if (current != null) current.setAvatarUrl(avatarUrl);
      return true;
    }
    return false;
  }

  public User refreshUser(int userId) {
    Request request = new Request(Request.REFRESH_USER, userId);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    if (response != null && Response.OK.equals(response.getStatus())) {
      return (User) response.getPayload();
    }
    return null;
  }

  public User getUserById(int userId) {
    Request request = new Request(Request.GET_USER_BY_ID, userId);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    if (response != null && Response.OK.equals(response.getStatus())) {
      return (User) response.getPayload();
    }
    return null;
  }
}
