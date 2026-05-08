package com.auction.server.handler.auction;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;

import com.auction.shared.BidTransaction;
import com.auction.shared.Request;
import com.auction.shared.Response;

public class BidHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    BidTransaction bid = (BidTransaction) request.getPayload();
    Response result = context.getAuctionManager().processBid(bid);
    return new Response(request.getRequestId(), result.getStatus(), result.getMessage(), result.getPayload());
  }
}
