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

/**
 * Xử lý luồng đặt giá (Bidding Pipeline).
 * Đã sửa lỗi tương thích với Optimistic Locking.
 */
final class AuctionBidPipeline {
  private final ItemDao itemdao;
  private final UserDao userdao;
  private final BidDao biddao;
  private final TransactionLogDao logdao;
  private final AuctionRealtimeNotifier notifier;
  private final BidAuctionValidator validator;
  private IntConsumer cleanupautobidsforitem = id -> {};

  AuctionBidPipeline(ItemDao itemdao, UserDao userdao, BidDao biddao, TransactionLogDao logdao,
                     AuctionRealtimeNotifier notifier, BidAuctionValidator validator) {
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
    if (item == null) return BidAuctionValidator.error("Item not found");

    // Đồng bộ giá cho đấu giá Hà Lan nếu cần
    DutchAuctionCatalogSync.syncItem(itemdao, item);
    item = itemdao.getById(bid.getItemId()); // Lấy lại bản ghi mới nhất kèm version mới

    User bidder = userdao.getById(String.valueOf(bid.getUserId()));
    Response valres = validator.validate(bid, item, bidder);
    if (valres != null) return valres;

    if (item.getAuctionType() == AuctionType.DUTCH) {
      return new DutchBiddingStrategy().process(bid, item, bidder, after, pendingpricebroadcast);
    }
    return new EnglishBiddingStrategy().process(bid, item, bidder, after, pendingpricebroadcast);
  }

  private class DutchBiddingStrategy {
    public Response process(BidTransaction bid, Item item, User bidder, List<Runnable> after, Set<Integer> pendingpricebroadcast) {
      double price = item.getCurrentPrice();
      if (Math.abs(bid.getBidValue() - price) > 0.02) return BidAuctionValidator.error("invalid_dutch_price");

      if (!userdao.atomicDeductBalance(bidder.getId(), price)) return BidAuctionValidator.error("insufficient_balance");

      logdao.insertLog(bidder.getId(), "ITEM_BOUGHT", -price, bid.getItemId());
      userdao.addBidderMetrics(bidder.getId(), price);

      if (!itemdao.atomicCloseAuction(item.getId(), bid.getUserId(), "CLOSED")) {
        userdao.atomicCreditBalance(bidder.getId(), price);
        logdao.insertLog(bidder.getId(), "BUY_REFUND", price, bid.getItemId());
        return BidAuctionValidator.error("auction_already_closed");
      }

      itemdao.updatePrice(item.getId(), price, item.getVersion());
      creditseller(item, price, after);

      after.add(() -> notifier.sendBalanceUpdateToUser(bidder.getId()));
      after.add(() -> notifier.broadcastItemClosed(item.getId()));
      after.add(() -> cleanupautobidsforitem.accept(item.getId()));

      return new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", bid.getItemId());
    }
  }

  private class EnglishBiddingStrategy {
    public Response process(BidTransaction bid, Item item, User bidder, List<Runnable> after, Set<Integer> pendingpricebroadcast) {
      if (item.getMaxPrice() > 0 && bid.getBidValue() >= item.getMaxPrice()) {
        return processbuyitnow(bid, item, bidder, after);
      }
      if (bid.getBidValue() <= item.getCurrentPrice()) return BidAuctionValidator.error("bid_too_low");

      try (Connection conn = com.auction.server.dao.platform.DatabaseConnection.getInstance().getConnection()) {
        conn.setAutoCommit(false);
        try {
          if (!userdao.deductBalanceTx(bidder.getId(), bid.getBidValue(), conn)) {
            conn.rollback();
            return BidAuctionValidator.error("insufficient_balance");
          }

          logdao.insertLogTx(bidder.getId(), "BID_HOLD", -bid.getBidValue(), bid.getItemId(), conn);
          int previd = biddao.getCurrentHighestBidderTx(bid.getItemId(), conn);
          double prevprice = item.getCurrentPrice();

          if (!biddao.placeBidTx(bid, conn)) {
            conn.rollback();
            return BidAuctionValidator.error("bid_failed");
          }

          // FIX: Thêm item.getVersion() vào đây
          if (!itemdao.updatePriceTx(item.getId(), bid.getBidValue(), item.getVersion(), conn)) {
            conn.rollback();
            return BidAuctionValidator.error("conflict_detected_try_again");
          }

          if (previd > 0 && prevprice > 0) {
            userdao.creditBalanceTx(previd, prevprice, conn);
            logdao.insertLogTx(previd, "BID_REFUND", prevprice, bid.getItemId(), conn);
            after.add(() -> notifier.sendBalanceUpdateToUser(previd));
            if (previd != bid.getUserId()) {
              after.add(() -> notifier.notifyOutbidUser(previd, item.getId()));
            }
          }

          conn.commit();
          applyantisnipeextension(item.getId());
          after.add(() -> notifier.sendBalanceUpdateToUser(bidder.getId()));
          pendingpricebroadcast.add(item.getId());
          return new Response("", Response.OK, "success", bid);
        } catch (Exception e) {
          conn.rollback();
          return BidAuctionValidator.error("db_transaction_error");
        }
      } catch (Exception e) {
        return BidAuctionValidator.error("db_connection_error");
      }
    }

    private Response processbuyitnow(BidTransaction bid, Item item, User bidder, List<Runnable> after) {
      double targetprice = item.getMaxPrice();
      if (!userdao.atomicDeductBalance(bidder.getId(), targetprice)) return BidAuctionValidator.error("insufficient_balance");

      logdao.insertLog(bidder.getId(), "ITEM_BOUGHT", -targetprice, bid.getItemId());
      userdao.addBidderMetrics(bidder.getId(), targetprice);

      if (!itemdao.atomicCloseAuction(item.getId(), bid.getUserId(), "CLOSED")) {
        userdao.atomicCreditBalance(bidder.getId(), targetprice);
        return BidAuctionValidator.error("auction_already_closed");
      }

      itemdao.updatePrice(item.getId(), targetprice, item.getVersion());
      creditseller(item, targetprice, after);
      after.add(() -> notifier.sendBalanceUpdateToUser(bidder.getId()));
      after.add(() -> notifier.broadcastItemClosed(item.getId()));
      after.add(() -> cleanupautobidsforitem.accept(item.getId()));
      return new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", bid.getItemId());
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