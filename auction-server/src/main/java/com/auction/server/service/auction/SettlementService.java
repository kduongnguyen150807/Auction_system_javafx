package com.auction.server.service.auction;

import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.server.dao.user.UserDao;
import com.auction.shared.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettlementService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SettlementService.class);

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
                LOGGER.warn("Settlement cycle failed", e);
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
