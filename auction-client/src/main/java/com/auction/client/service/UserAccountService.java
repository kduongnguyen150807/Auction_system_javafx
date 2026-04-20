package com.auction.client.service;

import com.auction.client.ClientSession;
import com.auction.client.network.NetworkClient;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserAccountService {
  private static final Logger LOGGER = Logger.getLogger(UserAccountService.class.getName());

  public boolean deposit(int userId, double amount) {
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

  public String updateProfile(int userId, String fullName, String email, String phone) {
    Map<String, String> data = new HashMap<>();
    data.put("userid", String.valueOf(userId));
    data.put("fullname", fullName);
    data.put("email", email);
    data.put("phone", phone);
    Request request = new Request(Request.UPDATE_PROFILE, (Serializable) data);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    if (response != null && Response.OK.equals(response.getStatus())) {
      return null;
    }
    return response != null ? response.getMessage() : "connection_error";
  }

  public boolean updateAvatar(String username, String avatarUrl) {
    Request request = new Request(Request.UPDATE_AVATAR, username + " " + avatarUrl);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    return response != null && Response.OK.equals(response.getStatus());
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
