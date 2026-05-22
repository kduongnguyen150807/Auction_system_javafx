package com.auction.server.handler.live;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.live.BidTierProvider;
import com.auction.server.live.LiveBidTierProvider;
import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import com.auction.shared.LiveBidTiers;
import com.auction.shared.Request;
import com.auction.shared.Response;

public class GetLiveBidTiersHandler implements ActionHandler {
  private final BidTierProvider tierProvider = new LiveBidTierProvider();

  @Override
  public Response handle(Request request, HandlerContext context) {
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
    LiveBidTiers tiers = tierProvider.compute(item.getCurrentPrice());
    return new Response(request.getRequestId(), Response.OK, "tiers", tiers);
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
