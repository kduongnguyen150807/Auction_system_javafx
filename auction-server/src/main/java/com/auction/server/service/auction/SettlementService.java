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
    int winnerid = bidDao.getPreviousHighestBidder(item.getId());
    if (winnerid > 0) {
      boolean closed = itemDao.atomicCloseAuction(item.getId(), winnerid, "CLOSED");
      if (!closed) {
        return;
      }
      double finalprice = item.getCurrentPrice();
      userDao.addBidderMetrics(winnerid, finalprice);
      User ans = userDao.getById(String.valueOf(winnerid));
      if (ans != null) {
        AuctionManager.getInstance().getLeaderboardservice().updatescore(winnerid, ans.getUsername(), ans.getAvatarUrl(), finalprice);
      }
      userDao.atomicCreditBalance(item.getSellerId(), finalprice);
      userDao.addSellerMetrics(item.getSellerId(), finalprice);
      logDao.insertLog(item.getSellerId(), "ITEM_SOLD", finalprice, item.getId());
      User freshseller = userDao.getById(String.valueOf(item.getSellerId()));
      if (freshseller != null) {
        AuctionManager.getInstance().sendToUser(item.getSellerId(), new Response("", "BALANCE_UPDATE", "Success", freshseller));
      }
      AuctionManager.getInstance().broadcastleaderboard();
    } else {
      boolean closed = itemDao.atomicCloseAuction(item.getId(), 0, "EXPIRED");
      if (!closed) {
        return;
      }
    }
    Item closeditem = itemDao.getById(item.getId());
    if (closeditem != null) {
      AuctionManager.getInstance().broadcast(new Response("", "ITEM_CLOSED", "closed", closeditem));
    }
  }}
