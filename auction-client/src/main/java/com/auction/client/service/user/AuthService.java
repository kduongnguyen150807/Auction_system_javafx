package com.auction.client.service.user;

import com.auction.client.navigation.SceneManager;
import com.auction.client.navigation.SceneType;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.util.FXThread; // Đảm bảo dùng helper bọc Platform.runLater
import com.auction.client.util.RequestHelper;
import com.auction.shared.PasswordEncoder;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AuthService {

  public CompletableFuture<Response> login(Map<?, ?> credentials) {
    if (credentials == null) {
      return CompletableFuture.failedFuture(new IllegalArgumentException("Thông tin đăng nhập không được để trống"));
    }
    Map<String, String> securedCredentials = new HashMap<>();

    String username = (String) credentials.get("username");
    String rawPassword = (String) credentials.get("password");
    String hashedPassword = PasswordEncoder.hash(rawPassword);

    securedCredentials.put("username", username);
    securedCredentials.put("password", hashedPassword);

    return RequestHelper.sendRequest(Request.LOGIN, securedCredentials)
      .thenApply(response -> {
        if (response != null && Response.OK.equals(response.getStatus())) {
          if (response.getPayload() instanceof User u) {
            ClientSession.CURRENT_SESSION.setUser(u);
          }
        }
        return response;
      });
  }

  public CompletableFuture<Response> register(User newUser) {
    String rawPassword = newUser.getPassword();
    String hashedPassword = PasswordEncoder.hash(rawPassword);
    newUser.setPassword(hashedPassword);

    return RequestHelper.sendRequest(Request.SIGNUP, newUser)
      .thenApply(response -> {
        if (response != null && Response.OK.equals(response.getStatus())) {
          if (response.getPayload() instanceof User u) {
            ClientSession.CURRENT_SESSION.setUser(u);
          }
        }
        return response;
      });
  }

  public void signOut() {
    ClientSession.CURRENT_SESSION.clear();
    FXThread.run(() -> {
      SceneManager.getInstance().deleteHomeView();
      SceneManager.getInstance().switchScene(SceneType.LOGIN_VIEW);
    });
  }

  public void switchHomeScene() {
    FXThread.run(() -> {
      SceneManager.getInstance().buildHomeView();
      SceneManager.getInstance().switchScene(SceneType.HOME_VIEW);
    });
  }
}