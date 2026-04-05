package com.auction.server.service;

import com.auction.server.dao.*;
import com.auction.shared.*;
import java.time.LocalDateTime;

public class BidService {
  private ItemDao itemDao;
  private BidDao bidDao;

  public BidService() {
    this.itemDao = new ItemDao();
    this.bidDao = new BidDao();
  }

  public Response placeBid(BidTransaction b) {
    Response ans = null;
    Item i = this.itemDao.getById(b.getItemId());
    if (i == null || i.getStatus() != ItemStatus.OPEN) {
      ans = new Response("sys", Response.ERROR, "closed", null);
      return ans;
    }
    if (b.getBidValue() <= i.getCurrentPrice()) {
      ans = new Response("sys", Response.ERROR, "low", null);
      return ans;
    }
    boolean res = this.bidDao.placeBid(b);
    if (res) {
      this.itemDao.updatePrice(i.getId(), b.getBidValue(), i.getVersion());
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime end = i.getEndTime();
      if (now.plusSeconds(30).isAfter(end)) {
        this.itemDao.updateEndTime(i.getId(), end.plusSeconds(60));
      }
      ans = new Response("sys", Response.OK, "success", b);
    } else {
      ans = new Response("sys", Response.ERROR, "conflict", null);
    }
    return ans;
  }
}
