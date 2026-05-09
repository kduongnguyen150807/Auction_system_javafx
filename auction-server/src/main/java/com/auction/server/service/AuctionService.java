package com.auction.server.service;

import com.auction.server.dao.LotDao;
import com.auction.shared.item.Item;
import com.auction.shared.linkv2.Response;

public class AuctionService {
  private final LotDao lotDao;

  public AuctionService(LotDao lotDao) {
    this.lotDao = lotDao;
  }

  public Response<Object> registerLot(String requestId, Item item, int sellerId) {
    boolean success = lotDao.registerLot(item, sellerId);
    if (success) {
      return Response.success(requestId,"success", null);
    } else {
      return Response.error(requestId, "fail");
    }
  }
}
