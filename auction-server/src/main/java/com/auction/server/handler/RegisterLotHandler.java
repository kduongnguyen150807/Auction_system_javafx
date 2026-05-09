package com.auction.server.handler;

import com.auction.server.context.HandlerContext;
import com.auction.server.service.AuctionService;
import com.auction.server.utils.RequestHandler;
import com.auction.shared.item.Item;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.Response;

public class RegisterLotHandler implements RequestHandler<Item, Object> {
  AuctionService auctionService;

  public RegisterLotHandler(AuctionService auctionService) {
    this.auctionService = auctionService;
  }

  @Override
  public Response<Object> handle(Request<Item> request, HandlerContext handlerContext) {
    return auctionService.registerLot(request.getId(), request.getData(), handlerContext.getUser().getId());
  }
}
