package com.auction.server.handler.auth;

import com.auction.server.context.HandlerContext;
import com.auction.server.service.AuthService;
import com.auction.server.utils.RequestHandler;
import com.auction.shared.dto.RegisterCredentials;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.Response;
import com.auction.shared.user.User;

public class RegisterHandler implements RequestHandler<RegisterCredentials, User> {
  private AuthService authService;

  public RegisterHandler(AuthService authService) {
    this.authService = authService;
  }

  @Override
  public Response<User> handle(Request<RegisterCredentials> request, HandlerContext handlerContext) throws Exception {
    return authService.register(request.getId(), request.getData(), handlerContext);
  }
}
