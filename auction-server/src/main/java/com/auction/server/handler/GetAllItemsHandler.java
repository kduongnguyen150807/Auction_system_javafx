package com.auction.server.handler;

import com.auction.server.context.HandlerContext;
import com.auction.server.service.AuctionService;
import com.auction.server.utils.RequestHandler;
import com.auction.shared.item.Item;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.Response;

import java.util.List;

public class GetAllItemsHandler implements RequestHandler<Object, List<Item>> {
  private final AuctionService auctionService;

  public GetAllItemsHandler(AuctionService auctionService) {
    this.auctionService = auctionService;
  }

  @Override
  public Response<List<Item>> handle(Request<Object> request, HandlerContext handlerContext) throws Exception {
    return auctionService.getAllItems(request.getId(), handlerContext);
  }
}
