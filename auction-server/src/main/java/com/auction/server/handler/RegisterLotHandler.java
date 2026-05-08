package com.auction.server.handler;

import com.auction.server.context.HandlerContext;
import com.auction.server.dao.LotDao;
import com.auction.server.utils.RequestHandler;
import com.auction.shared.item.Item;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.Response;

public class RegisterLotHandler implements RequestHandler<Item, Object> {
  @Override
  public Response<Object> handle(Request<Item> request, HandlerContext handlerContext) {
    Item item = request.getData();
    boolean success = handlerContext.getDaoContext().getDao(LotDao.class).registerLot(item, handlerContext.getUser().getId());
    if  (success) {
      return Response.success(request.getId(), "success", null);
    } else {
      return Response.error(request.getId(), "error");
    }
  }
}
