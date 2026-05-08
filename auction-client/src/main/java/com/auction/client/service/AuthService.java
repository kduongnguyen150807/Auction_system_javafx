package com.auction.client.service;

import com.auction.client.ClientSession;
import com.auction.client.ui.utils.RequestHelper;
import com.auction.shared.dto.LoginCredentials;
import com.auction.shared.linkv2.RequestType;
import com.auction.shared.linkv2.Response;
import com.auction.shared.linkv2.ResponseStatus;
import com.auction.shared.user.User;

import java.util.function.Consumer;

public final class AuthService {

  private static AuthService instance;

  private AuthService() {
  }

  public void login(LoginCredentials credentials,
                    Consumer<Response<User>> callback, Consumer<Throwable> errorCallback) {

    RequestHelper.sendRequest(RequestType.LOGIN, credentials,

      response -> handleLoginResponse((Response<User>) response, callback),

      errorCallback);
  }

  private void handleLoginResponse(Response<User> response, Consumer<Response<User>> callback) {

    if (response.getStatus() == ResponseStatus.SUCCESS) {

      User user = response.getData();

      ClientSession.setCurrentUser(user);
    }

    callback.accept(response);
  }

  public static AuthService getInstance() {

    if (instance == null) {
      instance = new AuthService();
    }

    return instance;
  }
}