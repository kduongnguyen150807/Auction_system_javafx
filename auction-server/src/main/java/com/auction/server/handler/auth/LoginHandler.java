package com.auction.server.handler.auth;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginHandler implements ActionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoginHandler.class);

  @Override
  public Response handle(Request request, HandlerContext context) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, String> credentials = (Map<String, String>) request.getPayload();

      if (credentials == null
              || isBlank(credentials.get("username"))
              || isBlank(credentials.get("password"))) {
        return new Response(request.getRequestId(), Response.ERROR, "missing_credentials", null);
      }

      String username = credentials.get("username").trim();
      String password = credentials.get("password");

      User user = context.getUserService().login(username, password);
      if (user != null) {
        String token = java.util.UUID.randomUUID().toString();
        user.setSessiontoken(token);
        context.getAuctionManager().registersession(token, user);
        context.setCurrentUser(user);

        return new Response(request.getRequestId(), Response.OK, "success", user);
      }

      User existingUser = context.getUserService().getByUsername(username);
      if (existingUser != null && existingUser.isLocked()) {
        return new Response(request.getRequestId(), Response.ERROR, "account_banned", null);
      }

      return new Response(request.getRequestId(), Response.ERROR, "fail", null);
    } catch (ClassCastException e) {
      LOGGER.warn("Login payload cast failed: {}", e.getMessage());
      return new Response(request.getRequestId(), Response.ERROR, "invalid_payload", null);
    } catch (Exception e) {
      LOGGER.error("Unexpected login error", e);
      return new Response(request.getRequestId(), Response.ERROR, "server_error", null);
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}