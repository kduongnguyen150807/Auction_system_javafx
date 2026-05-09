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
      String token = java.util.UUID.randomUUID().toString();
      user.setSessiontoken(token);
      context.getAuctionManager().registersession(token, user);
      context.setCurrentUser(user);
      Response ans = new Response(request.getRequestId(), Response.OK, "success", user);
      return ans;
    }

    User existinguser = new UserDao().getByUsername(username);
    if (existinguser != null && existinguser.isLocked()) {
      Response res = new Response(request.getRequestId(), Response.ERROR, "account_banned", null);
      return res;
    }
    Response res2 = new Response(request.getRequestId(), Response.ERROR, "fail", null);
    return res2;
  }
}