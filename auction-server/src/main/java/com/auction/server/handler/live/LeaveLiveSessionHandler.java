package com.auction.server.handler.live;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.live.LiveSessionManager;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;

public class LeaveLiveSessionHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    User user = context.getCurrentUser();
    if (user == null) {
      return new Response(request.getRequestId(), Response.ERROR, "auth_required", null);
    }
    LiveSessionManager.getInstance().leave(user.getId());
    return new Response(request.getRequestId(), Response.OK, "left", null);
  }
}
