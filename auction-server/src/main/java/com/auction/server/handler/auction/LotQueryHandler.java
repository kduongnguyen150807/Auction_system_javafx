package com.auction.server.handler.auction;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.service.auction.DutchAuctionCatalogSync;
import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.List;

public class LotQueryHandler implements ActionHandler {

  private static final int TRENDING_TOP_N = 5;

  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String requestId = request.getRequestId();
    Object payload = request.getPayload();

    List<?> results;
    switch (action) {
      case Request.GET_ONGOING_BIDS:
        results = syncedOngoing(context, parseUserId(payload));
        break;
      case Request.GET_TRENDING_LOTS:
        results = syncedTrending(context, payload);
        break;
      case Request.GET_UPCOMING_BIDS:
        results = syncedUpcoming(context, parseUserId(payload));
        break;
      case Request.GET_CLOSED_BIDS:
        results = syncedClosed(context, parseUserId(payload));
        break;
      case Request.GET_PAST_BIDS:
        results = syncedPast(context, parseUserId(payload));
        break;
      default:
        return new Response(requestId, Response.ERROR, "unknown_action", null);
    }
    return new Response(requestId, Response.OK, "success", (java.io.Serializable) results);
  }

  @SuppressWarnings("unchecked")
  private List<Item> syncedTrending(HandlerContext ctx, Object payload) {
    AuctionType kind = parseAuctionType(payload);
    List<Item> rows = (List<Item>) (List<?>) ctx.getLotDao().getTrendingLiveItems(kind, TRENDING_TOP_N);
    DutchAuctionCatalogSync.syncMany(ctx.getItemDao(), rows);
    return rows;
  }

  private static AuctionType parseAuctionType(Object payload) {
    if (payload instanceof AuctionType at) {
      return at;
    }
    if (payload != null) {
      return AuctionType.parse(payload.toString());
    }
    return AuctionType.ENGLISH;
  }

  private static int parseUserId(Object payload) {
    if (payload == null) {
      return 0;
    }
    if (payload instanceof Number n) {
      return n.intValue();
    }
    try {
      return Integer.parseInt(payload.toString().trim());
    } catch (NumberFormatException e) {
      return 0;
    }
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
