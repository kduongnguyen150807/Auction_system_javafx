package com.auction.server.handler.auction;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;

import com.auction.server.service.auction.DutchAuctionCatalogSync;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.Collections;
import java.util.List;

public class LotQueryHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String requestId = request.getRequestId();
    int userId = (int) request.getPayload();

    java.util.List<?> results;
    switch (action) {
      case Request.GET_ONGOING_BIDS:
        results = syncedOngoing(context, userId);
        break;
      case Request.GET_UPCOMING_BIDS:
        results = syncedUpcoming(context, userId);
        break;
      case Request.GET_CLOSED_BIDS:
        results = syncedClosed(context, userId);
        break;
      case Request.GET_PAST_BIDS:
        results = syncedPast(context, userId);
        break;
      default:
        return new Response(requestId, Response.ERROR, "unknown_action", null);
    }
    return new Response(requestId, Response.OK, "success", (java.io.Serializable) results);
  }

  @SuppressWarnings("unchecked")
  private List<Item> syncedOngoing(HandlerContext ctx, int userId) {
    List<Item> rows = (List<Item>) (List<?>) ctx.getLotDao().getOngoingBids(userId);
    DutchAuctionCatalogSync.syncMany(ctx.getItemDao(), rows);
    return rows;
  }

  @SuppressWarnings("unchecked")
  private List<Item> syncedUpcoming(HandlerContext ctx, int userId) {
    List<Item> rows = (List<Item>) (List<?>) ctx.getLotDao().getUpcomingBids(userId);
    DutchAuctionCatalogSync.syncMany(ctx.getItemDao(), rows);
    return rows;
  }

  @SuppressWarnings("unchecked")
  private List<Item> syncedClosed(HandlerContext ctx, int userId) {
    List<Item> rows = (List<Item>) (List<?>) ctx.getLotDao().getClosedBids(userId);
    DutchAuctionCatalogSync.syncMany(ctx.getItemDao(), rows);
    return rows;
  }

  @SuppressWarnings("unchecked")
  private List<Item> syncedPast(HandlerContext ctx, int userId) {
    List<Item> rows = (List<Item>) (List<?>) ctx.getLotDao().getPastBids(userId);
    DutchAuctionCatalogSync.syncMany(ctx.getItemDao(), rows);
    return rows;
  }
}
