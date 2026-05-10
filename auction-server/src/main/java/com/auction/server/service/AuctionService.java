package com.auction.server.service;

import com.auction.server.context.HandlerContext;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.LotDao;
import com.auction.shared.item.Item;
import com.auction.shared.item.ItemStatus;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.Response;

import java.util.ArrayList;
import java.util.List;

public class AuctionService {
  private final LotDao lotDao;
  private final ItemDao itemDao;

  public AuctionService(LotDao lotDao, ItemDao itemDao) {
    this.itemDao = itemDao;
    this.lotDao = lotDao;
  }

  public Response<List<Item>> getAllItems(String requestId, HandlerContext handlerContext) {
    List<Item> items = itemDao.getAllItems();
    if (items == null) {
      return Response.error(requestId, "fail to query items");
    } else {
      return Response.success(requestId, "successful query items" ,items);
    }
  }

  public Response<Boolean> approveItem(String requestId, HandlerContext handlerContext, Item item) {
    if (item.getStatus() != ItemStatus.PENDING) {
      return Response.error(requestId, "item is not pending");
    }
    boolean result = itemDao.approveItem(String.valueOf(item.getId()));
    return Response.success(requestId, "successful approve item", result);
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
