package com.auction.server.handler;

import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;

public class SignupHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    try {
      User user = (User) request.getPayload();
      boolean success = context.getUserService().signup(user);
      return new Response(request.getRequestId(),
          success ? Response.OK : Response.ERROR,
          success ? "success" : "duplicate", null);
    } catch (ClassCastException e) {
      e.printStackTrace();
      return new Response(request.getRequestId(), Response.ERROR, "class_cast_error", null);
    } catch (Exception e) {
      e.printStackTrace();
      return new Response(request.getRequestId(), Response.ERROR, "server_error", null);
    }
  }
}
