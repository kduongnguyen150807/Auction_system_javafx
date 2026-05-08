package com.auction.server.handler;

import com.auction.server.context.HandlerContext;
import com.auction.server.dao.LotDao;
import com.auction.server.utils.RequestHandler;
import com.auction.shared.Lot;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.Response;
import com.auction.shared.linkv2.ResponseStatus;

import java.util.List;

public class GetOnGoingBidHandler implements RequestHandler<Object, List<Lot>> {
  @Override
  public Response<List<Lot>> handle(Request<Object> request, HandlerContext handlerContext) {
    List<Lot> onGoingBids = handlerContext.getDaoContext().getDao(LotDao.class).getOngoingBids();
    return Response.success(request.getId(), "success", onGoingBids);
  }
}
