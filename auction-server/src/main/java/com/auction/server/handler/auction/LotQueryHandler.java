package com.auction.server.handler.auction;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;

import com.auction.shared.Request;
import com.auction.shared.Response;

public class LotQueryHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String requestId = request.getRequestId();
    int userId = (int) request.getPayload();

    java.util.List<?> results;
    switch (action) {
      case Request.GET_ONGOING_BIDS:
        results = context.getLotDao().getOngoingBids(userId);
        break;
      case Request.GET_UPCOMING_BIDS:
        results = context.getLotDao().getUpcomingBids(userId);
        break;
      case Request.GET_CLOSED_BIDS:
        results = context.getLotDao().getClosedBids(userId);
        break;
      case Request.GET_PAST_BIDS:
        results = context.getLotDao().getPastBids(userId);
        break;
      default:
        return new Response(requestId, Response.ERROR, "unknown_action", null);
    }
    return new Response(requestId, Response.OK, "success", (java.io.Serializable) results);
  }
}
