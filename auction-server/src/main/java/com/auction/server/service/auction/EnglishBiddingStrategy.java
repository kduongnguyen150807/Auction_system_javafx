package com.auction.server.service.auction;

import com.auction.server.dao.platform.DatabaseConnection;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.sql.Connection;
import java.util.List;
import java.util.Set;

final class EnglishBiddingStrategy implements BiddingStrategy {
  private final BiddingStrategyDependencies deps;

  EnglishBiddingStrategy(BiddingStrategyDependencies deps) {
    this.deps = deps;
  }

  @Override
  public Response process(
      BidTransaction bid, Item item, User bidder, List<Runnable> after, Set<Integer> pending) {
    if (item.getMaxPrice() > 0 && bid.getBidValue() >= item.getMaxPrice()) {
      return processBuyItNow(bid, item, bidder, after);
    }
    if (bid.getBidValue() <= item.getCurrentPrice()) {
      return BidAuctionValidator.error("bid_too_low");
    }

    try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
      conn.setAutoCommit(false);
      try {
        if (!deps.userDao.deductBalanceTx(bidder.getId(), bid.getBidValue(), conn)) {
          conn.rollback();
          return BidAuctionValidator.error("insufficient_balance");
        }

        deps.logDao.insertLogTx(bidder.getId(), "BID_HOLD", -bid.getBidValue(), bid.getItemId(), conn);
        int prevId = deps.bidDao.getCurrentHighestBidderTx(bid.getItemId(), conn);
        double prevPrice = item.getCurrentPrice();

        if (!deps.bidDao.placeBidTx(bid, conn)) {
          conn.rollback();
          return BidAuctionValidator.error("bid_failed");
        }

        if (!deps.itemDao.updatePriceTx(item.getId(), bid.getBidValue(), item.getVersion(), conn)) {
          conn.rollback();
          return BidAuctionValidator.error("conflict_detected_try_again");
        }

        if (prevId > 0 && prevPrice > 0) {
          deps.userDao.creditBalanceTx(prevId, prevPrice, conn);
          deps.logDao.insertLogTx(prevId, "BID_REFUND", prevPrice, bid.getItemId(), conn);
          after.add(() -> deps.notifier.sendBalanceUpdateToUser(prevId));
          if (prevId != bid.getUserId()) {
            after.add(() -> deps.notifier.notifyOutbidUser(prevId, item.getId()));
          }
        }

        conn.commit();
        BiddingSupport.applyAntiSnipeExtension(deps, item.getId());
        after.add(() -> deps.notifier.sendBalanceUpdateToUser(bidder.getId()));
        pending.add(item.getId());
        return new Response("", Response.OK, "success", bid);
      } catch (Exception e) {
        conn.rollback();
        return BidAuctionValidator.error("db_transaction_error");
      }
    } catch (Exception e) {
      return BidAuctionValidator.error("db_connection_error");
    }
  }

  private Response processBuyItNow(
      BidTransaction bid, Item item, User bidder, List<Runnable> after) {
    double targetPrice = item.getMaxPrice();
    if (!deps.userDao.atomicDeductBalance(bidder.getId(), targetPrice)) {
      return BidAuctionValidator.error("insufficient_balance");
    }

    deps.logDao.insertLog(bidder.getId(), "ITEM_BOUGHT", -targetPrice, bid.getItemId());
    deps.userDao.addBidderMetrics(bidder.getId(), targetPrice);

    if (!deps.itemDao.atomicCloseAuction(item.getId(), bid.getUserId(), "CLOSED")) {
      deps.userDao.atomicCreditBalance(bidder.getId(), targetPrice);
      return BidAuctionValidator.error("auction_already_closed");
    }

    deps.itemDao.updatePrice(item.getId(), targetPrice, item.getVersion());
    BiddingSupport.creditSeller(deps, item, targetPrice, after);

    after.add(() -> deps.notifier.sendBalanceUpdateToUser(bidder.getId()));
    after.add(() -> deps.notifier.broadcastItemClosed(item.getId()));
    int targetId = item.getId();
    after.add(() -> deps.cleanupAutoBidsForItem.accept(targetId));

    BiddingSupport.updateLeaderboard(deps, bidder, targetPrice);
    after.add(() -> AuctionManager.getInstance().broadcastleaderboard());

    return new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", bid.getItemId());
  }
}
