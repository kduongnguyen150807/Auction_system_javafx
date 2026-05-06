package com.auction.server.handler;

import com.auction.server.context.HandlerContext;
import com.auction.server.dao.LotDao;
import com.auction.server.utils.RequestHandler;
import com.auction.shared.Lot;
import com.auction.shared.link.Request;
import com.auction.shared.link.Response;

import java.util.List;

public class GetOnGoingBidHandler implements RequestHandler {
  @Override
  public Response handle(Request request, HandlerContext handlerContext) {
    List<Lot> onGoingBids = handlerContext.getDaoContext().getDao(LotDao.class).getOngoingBids();
    return new Response(request.getRequestId(), Response.OK, "success", onGoingBids);
  }
}
