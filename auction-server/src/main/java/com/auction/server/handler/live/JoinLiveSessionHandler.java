package com.auction.server.handler.live;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.live.LiveSessionManager;
import com.auction.server.live.VideoRelayServer;
import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.LiveSessionInfo;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.time.LocalDateTime;

public class JoinLiveSessionHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    User user = context.getCurrentUser();
    if (user == null) {
      return new Response(request.getRequestId(), Response.ERROR, "auth_required", null);
    }
    int itemId = toItemId(request.getPayload());
    if (itemId <= 0) {
      return new Response(request.getRequestId(), Response.ERROR, "invalid_item", null);
    }
    Item item = context.getItemDao().getById(itemId);
    if (item == null) {
      return new Response(request.getRequestId(), Response.ERROR, "Item not found", null);
    }
    if (item.getAuctionType() != AuctionType.LIVE) {
      return new Response(request.getRequestId(), Response.ERROR, "invalid_auction_type", null);
    }
    if (item.getStatus() != ItemStatus.OPEN) {
      return new Response(request.getRequestId(), Response.ERROR, "Auction is no longer open", null);
    }
    LocalDateTime now = LocalDateTime.now();
    if (item.getStartTime() != null && item.getStartTime().isAfter(now)) {
      return new Response(request.getRequestId(), Response.ERROR, "auction_not_started", null);
    }
    if (item.getEndTime() != null && item.getEndTime().isBefore(now)) {
      return new Response(request.getRequestId(), Response.ERROR, "Auction has ended", null);
    }

    int udpPort = VideoRelayServer.getInstance().getPort();
    LiveSessionInfo info = LiveSessionManager.getInstance().join(user, itemId, udpPort);
    return new Response(request.getRequestId(), Response.OK, "joined", info);
  }

  private static int toItemId(Object payload) {
    if (payload instanceof Integer i) {
      return i;
    }
    if (payload instanceof Number n) {
      return n.intValue();
    }
    return -1;
  }
}
