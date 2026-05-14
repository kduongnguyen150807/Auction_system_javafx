package com.auction.client.service;

import com.auction.client.ClientSession;
import com.auction.client.ui.loginview.LoginView;
import com.auction.client.ui.utils.FXThread;
import com.auction.client.ui.utils.RequestHelper;
import com.auction.shared.dto.LoginCredentials;
import com.auction.shared.dto.RegisterCredentials;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.RequestType;
import com.auction.shared.linkv2.Response;
import com.auction.shared.linkv2.ResponseStatus;
import com.auction.shared.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class AuthService {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoginView.class);

  private static AuthService instance;

  private AuthService() {
  }

  public CompletableFuture<ResponseStatus> login(LoginCredentials credentials) {
    return RequestHelper.<User>sendRequest(RequestType.LOGIN, credentials)
      .thenApply(response -> {
        if (response.isSuccess()) {
          handleAuth(response);
          return response.getStatus();
        } else {
          return response.getStatus();
        }
      });
  }

  public CompletableFuture<ResponseStatus> register(RegisterCredentials credentials) {
    return RequestHelper.<User>sendRequest(RequestType.REGISTER, credentials)
      .thenApply(response -> {
        if (response.isSuccess()) {
          handleAuth(response);
          return response.getStatus();
        } else {
          return response.getStatus();
        }
      });
  }

  private void handleAuth(Response<User> response) {
    if (response.getStatus() == ResponseStatus.SUCCESS) {
      User user = response.getData();
      LOGGER.info("User logged in successfully: {}, {} ", user.getUsername(), user.getRole()
        .name());
      ClientSession.setCurrentUser(user);
    }
  }


  public static AuthService getInstance() {

    if (instance == null) {
      instance = new AuthService();
    }

    return instance;
  }
}