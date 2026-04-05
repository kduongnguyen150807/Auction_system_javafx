package com.auction.server.service;

import com.auction.server.dao.*;
import com.auction.shared.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionCloser {
  private ItemDao itemDao;
  private BidDao bidDao;
  private ScheduledExecutorService scheduler;

  public AuctionCloser() {
    this.itemDao = new ItemDao();
    this.bidDao = new BidDao();
    this.scheduler = Executors.newScheduledThreadPool(1);
  }

  public void start() {
    Runnable task =
        () -> {
          try {
            List<Item> items = this.itemDao.getAll();
            LocalDateTime now = LocalDateTime.now();
            for (Item i : items) {
              if (i.getStatus() == ItemStatus.OPEN && i.getEndTime().isBefore(now)) {
                BidTransaction w = this.bidDao.getWinner(i.getId());
                if (w != null) {
                  this.itemDao.closeAuction(i.getId(), w.getUserId(), "FINISHED");
                  Response ans = new Response("sys", Response.OK, "closed", i.getId());
                  AuctionManager.getInstance().broadcast(ans);
                }
              }
            }
          } catch (Exception e) {
          }
        };
    this.scheduler.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
  }
}
