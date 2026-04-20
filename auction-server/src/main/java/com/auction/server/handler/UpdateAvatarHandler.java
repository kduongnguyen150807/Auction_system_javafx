package com.auction.server.handler;

import com.auction.shared.Request;
import com.auction.shared.Response;

public class UpdateAvatarHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    String[] parts = ((String) request.getPayload()).split(" ");
    try {
      context.getUserService().updateAvatar(parts[0], parts[1]);
      return new Response(request.getRequestId(), Response.OK, "success", null);
    } catch (Exception e) {
      return new Response(request.getRequestId(), Response.ERROR, "fail", null);
    }
  }
}
