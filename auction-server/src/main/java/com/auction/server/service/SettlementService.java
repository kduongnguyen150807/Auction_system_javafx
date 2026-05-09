package com.auction.server.service;

import com.auction.server.dao.BidDao;
import com.auction.server.dao.DatabaseConnection;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.TransactionLogDao;
import com.auction.server.dao.UserDao;
import com.auction.shared.Item;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettlementService {
  private static final Logger LOGGER = Logger.getLogger(SettlementService.class.getName());
  private static final int INITIAL_DELAY_SECONDS = 0;
  private static final int PERIOD_SECONDS = 10;

  private final ItemDao itemDao;
  private final UserDao userDao;
  private final TransactionLogDao logDao;
  private final BidDao bidDao;
  private final ScheduledExecutorService scheduler;

  private boolean started;

  public SettlementService() {
    this(new ItemDao(), new UserDao(), new TransactionLogDao(), new BidDao());
  }

  public SettlementService(
          ItemDao itemDao, UserDao userDao, TransactionLogDao logDao, BidDao bidDao) {
    this.itemDao = itemDao;
    this.userDao = userDao;
    this.logDao = logDao;
    this.bidDao = bidDao;
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
  }

  public synchronized void start() {
    if (started) {
      return;
    }

    scheduler.scheduleAtFixedRate(
            this::settleExpiredItemsSafely,
            INITIAL_DELAY_SECONDS,
            PERIOD_SECONDS,
            TimeUnit.SECONDS);

    started = true;
  }

  public synchronized void stop() {
    scheduler.shutdown();
    started = false;
  }

  private void settleExpiredItemsSafely() {
    try {
      List<Item> expiredItems = itemDao.getExpiredItems();
      for (Item item : expiredItems) {
        settle(item);
      }
    } catch (RuntimeException e) {
      LOGGER.log(Level.WARNING, "Failed to settle expired auctions", e);
    }
  }

  private void settle(Item item) {
    int winnerId = bidDao.getHighestBidderId(item.getId());

    if (winnerId > 0) {
      settleSoldItem(item, winnerId);
    } else {
      expireItemWithoutWinner(item);
    }

    AuctionManager.getInstance()
            .broadcast(new Response("", "ITEM_CLOSED", "Success", item.getId()));
  }

  private void settleSoldItem(Item item, int winnerId) {
    double finalPrice = item.getCurrentPrice();

    try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
      connection.setAutoCommit(false);

      try {
        boolean bidderMetricsUpdated = userDao.addBidderMetrics(connection, winnerId, 0);
        if (!bidderMetricsUpdated) {
          connection.rollback();
          return;
        }

        boolean sellerPaid = paySeller(connection, item, finalPrice);
        if (!sellerPaid) {
          connection.rollback();
          return;
        }

        itemDao.closeAuction(connection, item.getId(), winnerId, "CLOSED");
        connection.commit();
      } catch (Exception e) {
        connection.rollback();
        LOGGER.log(Level.WARNING, "Failed to settle sold item " + item.getId(), e);
      } finally {
        connection.setAutoCommit(true);
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to open settlement transaction", e);
    }
  }

  private boolean paySeller(Connection connection, Item item, double finalPrice) {
    User seller = userDao.getById(String.valueOf(item.getSellerId()));
    if (seller == null) {
      return true;
    }

    double newBalance = seller.getBalance() + finalPrice;

    boolean balanceUpdated = userDao.updateBalance(connection, seller.getId(), newBalance);
    boolean metricsUpdated = userDao.addSellerMetrics(connection, seller.getId(), finalPrice);
    boolean logInserted =
            logDao.insertLog(connection, seller.getId(), "ITEM_SOLD", finalPrice, item.getId());

    if (balanceUpdated && metricsUpdated && logInserted) {
      sendUpdatedBalance(seller.getId());
      return true;
    }

    return false;
  }

  private void expireItemWithoutWinner(Item item) {
    itemDao.closeAuction(item.getId(), 0, "EXPIRED");
  }

  private void sendUpdatedBalance(int userId) {
    AuctionManager.getInstance()
            .sendToUser(
                    userId,
                    new Response(
                            "",
                            "BALANCE_UPDATE",
                            "Success",
                            userDao.getById(String.valueOf(userId))));
  }
}