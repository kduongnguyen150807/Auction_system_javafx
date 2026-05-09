package com.auction.server.service;

import com.auction.server.context.HandlerContext;
import com.auction.server.dao.UserDao;
import com.auction.shared.dto.LoginCredentials;
import com.auction.shared.linkv2.Response;
import com.auction.shared.user.User;
import com.auction.shared.user.UserStatus;

public class AuthService {
  private final UserDao userDao;

  public AuthService(UserDao userDao) {
    this.userDao = userDao;
  }

  public Response<User> login(String requestId, LoginCredentials loginCredentials, HandlerContext handlerContext) {
    User user = userDao.login(loginCredentials.getUsername(), loginCredentials.getPassword());

    if (user == null) {
      return Response.error(
        requestId,
        "Invalid username or password"
      );
    }

    if (user.getStatus() == UserStatus.LOCKED) {
      return Response.error(
        requestId,
        "Account has been locked"
      );
    }

    handlerContext.setUser(user);

    return Response.success(requestId, "Login successful", user);
  }
}
