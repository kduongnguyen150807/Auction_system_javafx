package com.auction.server.handler;

import com.auction.server.context.HandlerContext;
import com.auction.server.service.AuthService;
import com.auction.server.utils.RequestHandler;
import com.auction.shared.dto.LoginCredentials;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.Response;
import com.auction.shared.user.User;

public class LoginHandler implements RequestHandler<LoginCredentials, User> {
  AuthService  authService;

  public LoginHandler(AuthService authService) {
    this.authService = authService;
  }

  @Override
  public Response<User> handle(Request<LoginCredentials> request, HandlerContext context) {
    return authService.login(request.getId(), request.getData(), context);
  }
}
