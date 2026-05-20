package com.auction.client.service.user;

import com.auction.client.navigation.SceneManager;
import com.auction.client.navigation.SceneType;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.util.RequestHelper;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;

import java.util.Map;

public class AuthService {
  public String login(Map<?, ?> credentials) {
    return RequestHelper.sendRequest(Request.LOGIN, credentials).thenApply(
      response -> {
        if (response == null) {
          return null;
        }
        if (response.getStatus().equals(Response.OK)) {
          if (response.getPayload() instanceof User u) {
            ClientSession.CURRENT_SESSION.setUser(u);
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
            ClientSession.CURRENT_SESSION.setUser(u);
            switchHomeScene();
          }
        }
        return response.getMessage();
      }
    ).join();
  }

  public void signOut() {
    ClientSession.CURRENT_SESSION.clear();
    SceneManager.getInstance().deleteHomeView();
    SceneManager.getInstance().switchScene(SceneType.LOGIN_VIEW);
  }

  public void switchHomeScene() {
    SceneManager.getInstance().buildHomeView();
    SceneManager.getInstance().switchScene(SceneType.HOME_VIEW);
  }
}
