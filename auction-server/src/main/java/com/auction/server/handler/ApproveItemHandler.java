package com.auction.server.handler;

import com.auction.server.context.HandlerContext;
import com.auction.server.service.AuctionService;
import com.auction.server.utils.Authorizable;
import com.auction.server.utils.RequestHandler;
import com.auction.shared.item.Item;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.Response;
import com.auction.shared.user.UserRole;

public class ApproveItemHandler implements RequestHandler<Item, Boolean>, Authorizable<Item> {
  private final AuctionService auctionService;

  public ApproveItemHandler(AuctionService auctionService) {
    this.auctionService = auctionService;
  }

  @Override
  public boolean authorize(HandlerContext handlerContext, Request<Item> request) {
    if (handlerContext.getUser().getRole() != UserRole.ADMIN) {
      return false;
    }
    return true;
  }


  @Override
  public Response<Boolean> handle(Request<Item> request, HandlerContext handlerContext) {
    return auctionService.approveItem(request.getId(), handlerContext, request.getData());
  }
}
