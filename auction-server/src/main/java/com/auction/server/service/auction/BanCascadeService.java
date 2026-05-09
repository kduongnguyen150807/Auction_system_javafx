package com.auction.server.service.auction;

import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.platform.DatabaseConnection;
import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.server.dao.user.UserDao;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BanCascadeService {
  private static final Logger LOGGER = LoggerFactory.getLogger(BanCascadeService.class);

  private final ItemDao itemDao;
  private final UserDao userDao;
  private final BidDao bidDao;
  private final TransactionLogDao logDao;
  private final AuctionManager manager;

  BanCascadeService(ItemDao itemDao, UserDao userDao, BidDao bidDao,
      TransactionLogDao logDao, AuctionManager manager) {
    this.itemDao = itemDao;
    this.userDao = userDao;
    this.bidDao = bidDao;
    this.logDao = logDao;
    this.manager = manager;
  }

  void handleBidderBan(int bidderId) {
    List<Integer> affected = bidDao.getOpenAuctionIdsForBidder(bidderId);
    LOGGER.info("Bidder ban: user={} affectedOpenAuctions={}", bidderId, affected.size());
    for (int itemId : affected) {
      ReentrantLock lock = manager.getAuctionLock(itemId);
      lock.lock();
      try {
        processBidderBan(itemId, bidderId);
      } catch (Exception e) {
        LOGGER.error("Bidder-ban failed for auction {}", itemId, e);
      } finally {
        lock.unlock();
      }
    }
  }

  void handleSellerBan(int sellerId) {
    List<Integer> affected = itemDao.getOpenAuctionIdsBySeller(sellerId);
    LOGGER.info("Seller ban: user={} affectedOpenAuctions={}", sellerId, affected.size());
    for (int itemId : affected) {
      ReentrantLock lock = manager.getAuctionLock(itemId);
      lock.lock();
      try {
        processSellerBan(itemId);
      } catch (Exception e) {
        LOGGER.error("Seller-ban failed for auction {}", itemId, e);
      } finally {
        lock.unlock();
      }
    }
  }

  private void processBidderBan(int itemId, int bidderId) throws SQLException {
    boolean[] refunded = {false};
    runTransaction(conn -> {
      Item item = itemDao.getByIdTx(itemId, conn);
      if (item == null || item.getStatus() != ItemStatus.OPEN) return;
      int currentLeader = bidDao.getCurrentHighestBidderTx(itemId, conn);
      double escrow = item.getCurrentPrice();
      int deleted = bidDao.deleteBidsByUserOnItemTx(itemId, bidderId, conn);
      LOGGER.info("Bidder ban: removed {} bid(s) on auction {} for user {}", deleted, itemId, bidderId);
      if (currentLeader == bidderId && deleted > 0) {
        userDao.creditBalanceTx(bidderId, escrow, conn);
        logDao.insertLogTx(bidderId, "BID_REFUND_BAN", escrow, itemId, conn);
        BidTransaction newLeader = bidDao.findHighestValidBidTx(itemId, conn);
        double newPrice = newLeader != null ? newLeader.getBidValue() : item.getStartingPrice();
        itemDao.updatePriceTx(itemId, newPrice, conn);
        LOGGER.info("Bidder ban: auction {} new leader={} new price={}", itemId,
            newLeader != null ? newLeader.getUserId() : -1, newPrice);
        refunded[0] = true;
      }
    });
    if (refunded[0]) {
      manager.sendBalanceUpdateToUser(bidderId);
      manager.broadcastPriceUpdate(itemId);
    }
  }

  private void processSellerBan(int itemId) throws SQLException {
    int[] refundedBidder = {-1};
    double[] refundedAmount = {0};
    runTransaction(conn -> {
      Item item = itemDao.getByIdTx(itemId, conn);
      if (item == null || item.getStatus() != ItemStatus.OPEN) return;
      int currentBidder = bidDao.getCurrentHighestBidderTx(itemId, conn);
      if (!itemDao.cancelAuctionTx(itemId, conn)) {
        LOGGER.warn("Seller ban: cancelAuctionTx returned false for item={}, may already be closed", itemId);
        return;
      }
      if (currentBidder > 0) {
        double held = item.getCurrentPrice();
        userDao.creditBalanceTx(currentBidder, held, conn);
        logDao.insertLogTx(currentBidder, "BID_REFUND_SELLER_BAN", held, itemId, conn);
        refundedBidder[0] = currentBidder;
        refundedAmount[0] = held;
      }
    });
    manager.cleanupAutoBids(itemId);
    if (refundedBidder[0] > 0) {
      manager.sendBalanceUpdateToUser(refundedBidder[0]);
      LOGGER.info("Seller ban: auction {} CANCELED, refunded bidder={} amount={}", itemId, refundedBidder[0], refundedAmount[0]);
    } else {
      LOGGER.info("Seller ban: auction {} CANCELED, no active bidder to refund", itemId);
    }
    manager.broadcastItemClosed(itemId);
  }

  @FunctionalInterface
  private interface SqlOp {
    void execute(Connection conn) throws SQLException;
  }

  private void runTransaction(SqlOp op) throws SQLException {
    Connection conn = DatabaseConnection.getInstance().getConnection();
    if (conn == null) throw new SQLException("No database connection available");
    try {
      conn.setAutoCommit(false);
      try {
        op.execute(conn);
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      }
    } finally {
      try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
      try { conn.close(); } catch (SQLException ignored) {}
    }
  }
}
