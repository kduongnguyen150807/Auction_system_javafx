package com.auction.server.handler.auth;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;

import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignupHandler implements ActionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(SignupHandler.class);

  @Override
  public Response handle(Request request, HandlerContext context) {
    try {
      User user = (User) request.getPayload();
      boolean success = context.getUserService().signup(user);
      return new Response(request.getRequestId(),
          success ? Response.OK : Response.ERROR,
          success ? "success" : "duplicate", null);
    } catch (ClassCastException e) {
      LOGGER.warn("Signup payload cast failed: {}", e.getMessage());
      return new Response(request.getRequestId(), Response.ERROR, "class_cast_error", null);
    } catch (Exception e) {
      LOGGER.error("Unexpected signup error", e);
      return new Response(request.getRequestId(), Response.ERROR, "server_error", null);
    }
  }
}
