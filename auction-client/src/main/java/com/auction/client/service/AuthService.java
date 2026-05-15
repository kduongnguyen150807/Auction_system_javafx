package com.auction.client.service;

import com.auction.client.ClientSession;
import com.auction.client.navigation.SceneManager;
import com.auction.client.navigation.SceneType;
import com.auction.client.network.NetworkClient;
import com.auction.client.util.RequestHelper;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;

import java.util.Map;

public class AuthService {
  private static AuthService instance;

  public static AuthService getInstance() {
    if (instance == null) {
      instance = new AuthService();
    }
    return instance;
  }

  public String login(Map<?, ?> credentials) {
    return RequestHelper.sendRequest(Request.LOGIN, credentials).thenApply(
      response -> {
        if (response == null) {
          return null;
        }
        if (response.getStatus().equals(Response.OK)) {
          if (response.getPayload() instanceof User u) {
            ClientSession.setCurrentUser(u);
            switchHomeScene();
          }
        }
        return response.getMessage();
      }
    ).join();
  }

  public String register(User newUser) {
    return RequestHelper.sendRequest(Request.SIGNUP, newUser).thenApply(
      response -> {
        if (response == null) {
          return null;
        }
        if (response.getStatus().equals(Response.OK)) {
          if (response.getPayload() instanceof User u) {
            ClientSession.setCurrentUser(u);
            switchHomeScene();
          }
        }
        return response.getMessage();
      }
    ).join();
  }

  public void signOut() {
    ClientSession.clear();
    switchLoginScene();
  }

  public void switchHomeScene() {
    SceneManager.getInstance().buildHomeView();
    SceneManager.getInstance().switchScene(SceneType.HOME_VIEW);
  }

  public void switchLoginScene() {
    SceneManager.getInstance().switchScene(SceneType.LOGIN_VIEW);
  }
}
