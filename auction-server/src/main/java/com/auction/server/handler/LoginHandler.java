package com.auction.server.handler;

import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.Map;

public class LoginHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    Map<String, String> credentials = (Map<String, String>) request.getPayload();
    User user = context.getUserService().login(credentials.get("username"), credentials.get("password"));
    if (user != null) {
      context.setCurrentUser(user);
      return new Response(request.getRequestId(), Response.OK, "success", user);
    }
    return new Response(request.getRequestId(), Response.ERROR, "fail", null);
  }
}
