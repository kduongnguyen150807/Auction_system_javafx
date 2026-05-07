package com.auction.server.handler;

import com.auction.server.context.HandlerContext;
import com.auction.server.dao.LotDao;
import com.auction.server.utils.RequestHandler;
import com.auction.shared.item.Item;
import com.auction.shared.link.Request;
import com.auction.shared.link.Response;

public class RegisterLotHandler implements RequestHandler {
  @Override
  public Response handle(Request request, HandlerContext handlerContext) {
    Item item = (Item) request.getPayload();
    boolean success = handlerContext.getDaoContext().getDao(LotDao.class).registerLot(item, handlerContext.getUser().getId());
    if  (success) {
      return new Response(request.getRequestId(), Response.OK, "success", null);
    } else {
      return new Response(request.getRequestId(), Response.ERROR, "error", null);
    }
  }
}
