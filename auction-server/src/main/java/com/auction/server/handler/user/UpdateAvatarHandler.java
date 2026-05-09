package com.auction.server.handler.user;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;

public class UpdateAvatarHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    String[] parts = ((String) request.getPayload()).split(" ");
    try {
      context.getUserService().updateAvatar(parts[0], parts[1]);
      User ans = context.getCurrentUser();
      if (ans != null && ans.getUsername().equals(parts[0])) {
        context.getAuctionManager().getLeaderboardservice().updateuserinfo(ans.getId(), parts[1]);
        context.getAuctionManager().broadcastleaderboard();
      }
      Response res = new Response(request.getRequestId(), Response.OK, "success", null);
      return res;
    } catch (Exception e) {
      Response res = new Response(request.getRequestId(), Response.ERROR, "fail", null);
      return res;
    }
  }
}