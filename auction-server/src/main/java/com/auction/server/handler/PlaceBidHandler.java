package com.auction.server.handler;

import com.auction.server.context.HandlerContext;
import com.auction.server.service.AuctionService;
import com.auction.server.utils.Authorizable;
import com.auction.server.utils.RequestHandler;
import com.auction.shared.BidResult;
import com.auction.shared.dto.BidForm;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.Response;

public class PlaceBidHandler implements RequestHandler<BidForm, BidResult>, Authorizable<BidForm> {
  AuctionService auctionService;

  public PlaceBidHandler(AuctionService auctionService) {
    this.auctionService = auctionService;
  }

  @Override
  public Response<BidResult> handle(Request<BidForm> request, HandlerContext handlerContext) throws Exception {
    return auctionService.submitBid(request.getId(), request.getData(), handlerContext).join();
  }

  @Override
  public boolean authorize(HandlerContext handlerContext, Request<BidForm> request) {
    BidForm bidForm = request.getData();
    if ( bidForm.getBidderId() !=  handlerContext.getUser().getId() ) {
      return false;
    }
    return true;
  }
}
