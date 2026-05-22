package com.auction.client.service.user;

import com.auction.client.navigation.SceneManager;
import com.auction.client.navigation.SceneType;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.lotsinformation.ClosedLots;
import com.auction.client.store.lotsinformation.OngoingLots;
import com.auction.client.store.lotsinformation.PendingLots;
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

    OngoingLots.AUCTION_STORE.clear();
    ClosedLots.CLOSED_LOTS.clear();
    PendingLots.PENDING_LOTS.clear();

    FXThread.run(() -> {
      SceneManager.getInstance().buildLoginView();
      SceneManager.getInstance().switchScene(SceneType.LOGIN_VIEW);
      SceneManager.getInstance().deleteHomeView();
    });
  }

  public void switchHomeScene() {
    FXThread.run(() -> {
      SceneManager.getInstance().buildHomeView();
      SceneManager.getInstance().switchScene(SceneType.HOME_VIEW);
      SceneManager.getInstance().deleteLoginView();
    });
  }
}