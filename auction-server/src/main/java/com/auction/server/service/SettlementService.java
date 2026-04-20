package com.auction.server.service;

import com.auction.server.dao.BidDao;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.TransactionLogDao;
import com.auction.server.dao.UserDao;
import com.auction.shared.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettlementService {
  private static final Logger LOGGER = Logger.getLogger(SettlementService.class.getName());

  private final ItemDao itemDao;
  private final UserDao userDao;
  private final BidDao bidDao;
  private final TransactionLogDao logDao;

  public SettlementService() {
    this.itemDao = new ItemDao();
    this.userDao = new UserDao();
    this.bidDao = new BidDao();
    this.logDao = new TransactionLogDao();
  }

  public void start() {
    Executors.newSingleThreadScheduledExecutor()
        .scheduleAtFixedRate(
            () -> {
              try {
                List<Item> expiredItems = itemDao.getExpiredItems();
                for (Item item : expiredItems) {
                  settle(item);
                }
              } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Settlement cycle failed", e);
              }
            },
            5, 10, TimeUnit.SECONDS);
  }

  private void settle(Item item) {
    int winnerId = bidDao.getPreviousHighestBidder(item.getId());

    if (winnerId > 0) {
      boolean closed = itemDao.atomicCloseAuction(item.getId(), winnerId, "CLOSED");
      if (!closed) return;

      double finalPrice = item.getCurrentPrice();
      userDao.addBidderMetrics(winnerId, 0);

      userDao.atomicCreditBalance(item.getSellerId(), finalPrice);
      userDao.addSellerMetrics(item.getSellerId(), finalPrice);
      logDao.insertLog(item.getSellerId(), "ITEM_SOLD", finalPrice, item.getId());

      User freshSeller = userDao.getById(String.valueOf(item.getSellerId()));
      if (freshSeller != null) {
        AuctionManager.getInstance().sendToUser(item.getSellerId(),
            new Response("", "BALANCE_UPDATE", "Success", freshSeller));
      }
    } else {
      boolean closed = itemDao.atomicCloseAuction(item.getId(), 0, "EXPIRED");
      if (!closed) return;
    }

    Item closedItem = itemDao.getById(item.getId());
    if (closedItem != null) {
      AuctionManager.getInstance().broadcast(
          new Response("", "ITEM_CLOSED", "closed", closedItem));
    }
  }
}
