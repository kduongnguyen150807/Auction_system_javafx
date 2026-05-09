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

interface BiddingStrategy {
  Response process(BidTransaction bid, Item item, User bidder, List<Runnable> after, Set<Integer> pendingpricebroadcast);
}

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
    Response valres = validator.validate(bid, item, bidder);
    if (valres != null) {
      return valres;
    }
    BiddingStrategy strategy = getstrategy(item);
    Response ans = strategy.process(bid, item, bidder, after, pendingpricebroadcast);
    return ans;
  }

  private BiddingStrategy getstrategy(Item item) {
    if (item != null && item.getAuctionType() == AuctionType.DUTCH) {
      return new DutchBiddingStrategy();
    }
    return new EnglishBiddingStrategy();
  }

  private class DutchBiddingStrategy implements BiddingStrategy {
    @Override
    public Response process(BidTransaction bid, Item item, User bidder, List<Runnable> after, Set<Integer> pendingpricebroadcast) {
      double price = item.getCurrentPrice();
      double diff = Math.abs(bid.getBidValue() - price);
      if (diff > 0.02) {
        Response res = BidAuctionValidator.error("invalid_dutch_price");
        return res;
      }
      boolean deductres = userdao.atomicDeductBalance(bidder.getId(), price);
      if (!deductres) {
        Response res = BidAuctionValidator.error("insufficient_balance");
        return res;
      }
      logdao.insertLog(bidder.getId(), "ITEM_BOUGHT", -price, bid.getItemId());
      userdao.addBidderMetrics(bidder.getId(), price);
      boolean closeres = itemdao.atomicCloseAuction(item.getId(), bid.getUserId(), "CLOSED");
      if (!closeres) {
        userdao.atomicCreditBalance(bidder.getId(), price);
        logdao.insertLog(bidder.getId(), "BUY_REFUND", price, bid.getItemId());
        Response res = BidAuctionValidator.error("auction_already_closed");
        return res;
      }
      itemdao.updatePrice(item.getId(), price, item.getVersion());
      creditseller(item, price, after);
      after.add(() -> notifier.sendBalanceUpdateToUser(bidder.getId()));
      after.add(() -> notifier.broadcastItemClosed(item.getId()));
      int targetid = item.getId();
      after.add(() -> cleanupautobidsforitem.accept(targetid));
      AuctionManager.getInstance().getLeaderboardservice().updatescore(bidder.getId(), bidder.getUsername(), bidder.getAvatarUrl(), price);
      after.add(() -> AuctionManager.getInstance().broadcastleaderboard());
      Response ans = new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", bid.getItemId());
      return ans;
    }
  }

  private class EnglishBiddingStrategy implements BiddingStrategy {
    @Override
    public Response process(BidTransaction bid, Item item, User bidder, List<Runnable> after, Set<Integer> pendingpricebroadcast) {
      if (item.getMaxPrice() > 0 && bid.getBidValue() >= item.getMaxPrice()) {
        Response ans = processbuyitnow(bid, item, bidder, after);
        return ans;
      }
      if (bid.getBidValue() <= item.getCurrentPrice()) {
        Response ans = BidAuctionValidator.error("bid_too_low");
        return ans;
      }
      try (Connection conn = com.auction.server.dao.platform.DatabaseConnection.getInstance().getConnection()) {
        conn.setAutoCommit(false);
        try {
          boolean deductres = userdao.deductBalanceTx(bidder.getId(), bid.getBidValue(), conn);
          if (!deductres) {
            conn.rollback();
            Response ans = BidAuctionValidator.error("insufficient_balance");
            return ans;
          }
          logdao.insertLogTx(bidder.getId(), "BID_HOLD", -bid.getBidValue(), bid.getItemId(), conn);
          int previd = biddao.getCurrentHighestBidderTx(bid.getItemId(), conn);
          double prevprice = item.getCurrentPrice();
          boolean placeres = biddao.placeBidTx(bid, conn);
          if (!placeres) {
            conn.rollback();
            Response ans = BidAuctionValidator.error("bid_failed");
            return ans;
          }
          itemdao.updatePriceTx(item.getId(), bid.getBidValue(), conn);
          if (previd > 0 && prevprice > 0) {
            userdao.creditBalanceTx(previd, prevprice, conn);
            logdao.insertLogTx(previd, "BID_REFUND", prevprice, bid.getItemId(), conn);
            after.add(() -> notifier.sendBalanceUpdateToUser(previd));
            int targetid = bid.getItemId();
            after.add(() -> notifier.notifyOutbidUser(previd, targetid));
          }
          conn.commit();
          after.add(() -> applyantisnipeextension(bid.getItemId()));
          after.add(() -> notifier.sendBalanceUpdateToUser(bidder.getId()));
          pendingpricebroadcast.add(bid.getItemId());
          Response ans = new Response("", Response.OK, "success", bid);
          return ans;
        } catch (Exception e) {
          conn.rollback();
          Response ans = BidAuctionValidator.error("db_transaction_error");
          return ans;
        }
      } catch (Exception e) {
        Response ans = BidAuctionValidator.error("db_connection_error");
        return ans;
      }
    }

    private Response processbuyitnow(BidTransaction bid, Item item, User bidder, List<Runnable> after) {
      double targetprice = item.getMaxPrice();
      boolean deductres = userdao.atomicDeductBalance(bidder.getId(), targetprice);
      if (!deductres) {
        Response ans = BidAuctionValidator.error("insufficient_balance");
        return ans;
      }
      logdao.insertLog(bidder.getId(), "ITEM_BOUGHT", -targetprice, bid.getItemId());
      userdao.addBidderMetrics(bidder.getId(), targetprice);
      boolean closeres = itemdao.atomicCloseAuction(item.getId(), bid.getUserId(), "CLOSED");
      if (!closeres) {
        userdao.atomicCreditBalance(bidder.getId(), targetprice);
        logdao.insertLog(bidder.getId(), "BUY_REFUND", targetprice, bid.getItemId());
        Response ans = BidAuctionValidator.error("auction_already_closed");
        return ans;
      }
      itemdao.updatePrice(item.getId(), targetprice, item.getVersion());
      creditseller(item, targetprice, after);
      after.add(() -> notifier.sendBalanceUpdateToUser(bidder.getId()));
      after.add(() -> notifier.broadcastItemClosed(item.getId()));
      int targetid = item.getId();
      after.add(() -> cleanupautobidsforitem.accept(targetid));
      AuctionManager.getInstance().getLeaderboardservice().updatescore(bidder.getId(), bidder.getUsername(), bidder.getAvatarUrl(), targetprice);
      after.add(() -> AuctionManager.getInstance().broadcastleaderboard());
      Response ans = new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", bid.getItemId());
      return ans;
    }
  }

  private void creditseller(Item item, double amount, List<Runnable> after) {
    userdao.atomicCreditBalance(item.getSellerId(), amount);
    userdao.addSellerMetrics(item.getSellerId(), amount);
    logdao.insertLog(item.getSellerId(), "ITEM_SOLD", amount, item.getId());
    after.add(() -> notifier.sendBalanceUpdateToUser(item.getSellerId()));
  }

  private void applyantisnipeextension(int itemid) {
    Item item = itemdao.getById(itemid);
    if (item != null && item.getEndTime() != null) {
      long diff = Duration.between(LocalDateTime.now(), item.getEndTime()).getSeconds();
      if (diff < 60) {
        LocalDateTime newend = item.getEndTime().plusSeconds(60);
        itemdao.updateEndTime(item.getId(), newend);
        SettlementService.getInstance().schedule(item.getId(), newend);
      }
    }
  }
}