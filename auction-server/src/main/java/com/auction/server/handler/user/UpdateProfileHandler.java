package com.auction.server.handler.user;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;

import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.Map;

public class UpdateProfileHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    Map<String, String> data = (Map<String, String>) request.getPayload();
    String error = context.getUserService().updateProfile(
        Integer.parseInt(data.get("userid")),
        data.get("fullname"),
        data.get("email"),
        data.get("phone"));
    return new Response(request.getRequestId(),
        error == null ? Response.OK : Response.ERROR,
        error == null ? "success" : error, null);
  }
}
