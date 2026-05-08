package com.auction.server.handler.auth;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;

import com.auction.server.dao.user.UserDao;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.Map;

public class LoginHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    Map<String, String> credentials = (Map<String, String>) request.getPayload();
    String username = credentials.get("username");
    String password = credentials.get("password");

    User user = context.getUserService().login(username, password);
    if (user != null) {
      context.setCurrentUser(user);
      return new Response(request.getRequestId(), Response.OK, "success", user);
    }

    // Distinguish "wrong credentials" from "account suspended" so the client
    // can show a meaningful message rather than a generic "incorrect password".
    User existingUser = new UserDao().getByUsername(username);
    if (existingUser != null && existingUser.isLocked()) {
      return new Response(request.getRequestId(), Response.ERROR, "account_banned", null);
    }
    return new Response(request.getRequestId(), Response.ERROR, "fail", null);
  }
}
