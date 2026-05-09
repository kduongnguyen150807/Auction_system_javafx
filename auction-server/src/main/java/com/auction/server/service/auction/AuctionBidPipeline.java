package com.auction.server.service.auction;

import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.user.UserDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.shared.AuctionType;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.sql.Connection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

final class AuctionBidPipeline {
  private final ItemDao itemdao;
  private final UserDao userdao;
  private final BidDao biddao;
  private final TransactionLogDao logdao;
  private final AuctionRealtimeNotifier notifier;
  private final BidAuctionValidator validator;
  private IntConsumer cleanupautobidsforitem = id -> {};

  AuctionBidPipeline(ItemDao itemdao, UserDao userdao, BidDao biddao, TransactionLogDao logdao, AuctionRealtimeNotifier notifier, BidAuctionValidator validator) {
    this.itemdao = itemdao;
    this.userdao = userdao;
    this.biddao = biddao;
    this.logdao = logdao;
    this.notifier = notifier;
    this.validator = validator;
  }

  void setCleanupAutoBidsForItem(IntConsumer cleanupautobidsforitem) {
    if (cleanupautobidsforitem != null) {
      this.cleanupautobidsforitem = cleanupautobidsforitem;
    }
  }

  Response processManualBid(BidTransaction bid, List<Runnable> after, Set<Integer> pendingpricebroadcast) {
    Item item = itemdao.getById(bid.getItemId());
    DutchAuctionCatalogSync.syncItem(itemdao, item);
    item = itemdao.getById(bid.getItemId());
    User bidder = userdao.getById(String.valueOf(bid.getUserId()));
    Response ans = validator.validate(bid, item, bidder);
    if (ans != null) {
      return ans;
    }
    if (item != null && item.getAuctionType() == AuctionType.DUTCH) {
      return processDutchPurchase(bid, item, bidder, after);
    }
    if (isBuyItNow(item, bid)) {
      return processBuyItNow(bid, item, bidder, after);
    }
    return processRegularBid(bid, item, bidder, after, pendingpricebroadcast);
  }

  private boolean isBuyItNow(Item item, BidTransaction bid) {
    boolean ans = item.getMaxPrice() > 0 && bid.getBidValue() >= item.getMaxPrice();
    return ans;
  }

  private Response processDutchPurchase(BidTransaction bid, Item item, User bidder, List<Runnable> after) {
    double price = item.getCurrentPrice();
    double ans = Math.abs(bid.getBidValue() - price);
    if (ans > 0.02) {
      return BidAuctionValidator.error("error");
    }
    boolean res = userdao.atomicDeductBalance(bidder.getId(), price);
    if (!res) {
      return BidAuctionValidator.error("error");
    }
    logdao.insertLog(bidder.getId(), "ITEM_BOUGHT", -price, bid.getItemId());
    userdao.addBidderMetrics(bidder.getId(), price);
    boolean res2 = itemdao.atomicCloseAuction(item.getId(), bid.getUserId(), "CLOSED");
    if (!res2) {
      userdao.atomicCreditBalance(bidder.getId(), price);
      logdao.insertLog(bidder.getId(), "BUY_REFUND", price, bid.getItemId());
      return BidAuctionValidator.error("error");
    }
    itemdao.updatePrice(item.getId(), price, item.getVersion());
    creditSeller(item, price, after);
    after.add(() -> notifier.sendBalanceUpdateToUser(bidder.getId()));
    after.add(() -> notifier.broadcastItemClosed(item.getId()));
    int closeditemid = item.getId();
    after.add(() -> cleanupautobidsforitem.accept(closeditemid));
    AuctionManager.getInstance().getLeaderboardservice().updatescore(bidder.getId(), bidder.getUsername(), bidder.getAvatarUrl(), price);
    after.add(() -> AuctionManager.getInstance().broadcastleaderboard());
    Response res3 = new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", bid.getItemId());
    return res3;
  }

  private Response processBuyItNow(BidTransaction bid, Item item, User bidder, List<Runnable> after) {
    double p = item.getMaxPrice();
    boolean ans = userdao.atomicDeductBalance(bidder.getId(), p);
    if (!ans) {
      return BidAuctionValidator.error("error");
    }
    logdao.insertLog(bidder.getId(), "ITEM_BOUGHT", -p, bid.getItemId());
    userdao.addBidderMetrics(bidder.getId(), p);
    boolean res = itemdao.atomicCloseAuction(item.getId(), bid.getUserId(), "CLOSED");
    if (!res) {
      userdao.atomicCreditBalance(bidder.getId(), p);
      logdao.insertLog(bidder.getId(), "BUY_REFUND", p, bid.getItemId());
      return BidAuctionValidator.error("error");
    }
    itemdao.updatePrice(item.getId(), p, item.getVersion());
    creditSeller(item, p, after);
    after.add(() -> notifier.sendBalanceUpdateToUser(bidder.getId()));
    after.add(() -> notifier.broadcastItemClosed(item.getId()));
    int closeditemid = item.getId();
    after.add(() -> cleanupautobidsforitem.accept(closeditemid));
    AuctionManager.getInstance().getLeaderboardservice().updatescore(bidder.getId(), bidder.getUsername(), bidder.getAvatarUrl(), p);
    after.add(() -> AuctionManager.getInstance().broadcastleaderboard());
    Response res2 = new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", bid.getItemId());
    return res2;
  }

  private Response processRegularBid(BidTransaction bid, Item item, User bidder, List<Runnable> after, Set<Integer> pendingpricebroadcast) {
    if (bid.getBidValue() <= item.getCurrentPrice()) {
      return BidAuctionValidator.error("error");
    }
    try (Connection conn = com.auction.server.dao.platform.DatabaseConnection.getInstance().getConnection()) {
      conn.setAutoCommit(false);
      try {
        boolean ans = userdao.deductBalanceTx(bidder.getId(), bid.getBidValue(), conn);
        if (!ans) {
          conn.rollback();
          return BidAuctionValidator.error("error");
        }
        logdao.insertLogTx(bidder.getId(), "BID_HOLD", -bid.getBidValue(), bid.getItemId(), conn);
        int previd = biddao.getCurrentHighestBidderTx(bid.getItemId(), conn);
        double prevprice = item.getCurrentPrice();
        boolean res = biddao.placeBidTx(bid, conn);
        if (!res) {
          conn.rollback();
          return BidAuctionValidator.error("error");
        }
        itemdao.updatePriceTx(item.getId(), bid.getBidValue(), conn);
        if (previd > 0 && prevprice > 0) {
          userdao.creditBalanceTx(previd, prevprice, conn);
          logdao.insertLogTx(previd, "BID_REFUND", prevprice, bid.getItemId(), conn);
          after.add(() -> notifier.sendBalanceUpdateToUser(previd));
          int outbiditemid = bid.getItemId();
          after.add(() -> notifier.notifyOutbidUser(previd, outbiditemid));
        }
        conn.commit();
        after.add(() -> applyAntiSnipeExtension(bid.getItemId()));
        after.add(() -> notifier.sendBalanceUpdateToUser(bidder.getId()));
        pendingpricebroadcast.add(bid.getItemId());
        Response res2 = new Response("", Response.OK, "success", bid);
        return res2;
      } catch (Exception e) {
        conn.rollback();
        return BidAuctionValidator.error("error");
      }
    } catch (Exception e) {
      return BidAuctionValidator.error("error");
    }
  }

  private void creditSeller(Item item, double amount, List<Runnable> after) {
    userdao.atomicCreditBalance(item.getSellerId(), amount);
    userdao.addSellerMetrics(item.getSellerId(), amount);
    logdao.insertLog(item.getSellerId(), "ITEM_SOLD", amount, item.getId());
    after.add(() -> notifier.sendBalanceUpdateToUser(item.getSellerId()));
  }

  private void applyAntiSnipeExtension(int itemid) {
    Item item = itemdao.getById(itemid);
    if (item != null && item.getEndTime() != null) {
      long ans = Duration.between(LocalDateTime.now(), item.getEndTime()).getSeconds();
      if (ans < 60) {
        LocalDateTime res = item.getEndTime().plusSeconds(60);
        itemdao.updateEndTime(item.getId(), res);
        SettlementService.getInstance().schedule(item.getId(), res);
      }
    }
  }
}