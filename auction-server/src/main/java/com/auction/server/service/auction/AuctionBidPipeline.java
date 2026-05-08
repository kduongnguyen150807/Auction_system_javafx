package com.auction.server.service.auction;

import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.server.dao.user.UserDao;
import com.auction.shared.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.IntConsumer;

/**
 * Applies manual bids (buy-it-now vs incremental), escrow/refunds, and schedules realtime side-effects.
 */
final class AuctionBidPipeline {

  private static final long ANTI_SNIPE_THRESHOLD_SECONDS = 60;

  private final ItemDao itemDao;
  private final UserDao userDao;
  private final BidDao bidDao;
  private final TransactionLogDao logDao;
  private final AuctionRealtimeNotifier notifier;
  private final BidAuctionValidator validator;

  /** Clears auto-bid registry when an auction closes (e.g. buy-it-now). */
  private IntConsumer cleanupAutoBidsForItem = itemId -> {};

  AuctionBidPipeline(
      ItemDao itemDao,
      UserDao userDao,
      BidDao bidDao,
      TransactionLogDao logDao,
      AuctionRealtimeNotifier notifier,
      BidAuctionValidator validator) {
    this.itemDao = itemDao;
    this.userDao = userDao;
    this.bidDao = bidDao;
    this.logDao = logDao;
    this.notifier = notifier;
    this.validator = validator;
  }

  void setCleanupAutoBidsForItem(IntConsumer cleanupAutoBidsForItem) {
    this.cleanupAutoBidsForItem = cleanupAutoBidsForItem != null ? cleanupAutoBidsForItem : id -> {};
  }

  Response processManualBid(BidTransaction bid, List<Runnable> after, Set<Integer> pendingPriceBroadcast) {
    Item item = itemDao.getById(bid.getItemId());
    User bidder = userDao.getById(String.valueOf(bid.getUserId()));
    Response validation = validator.validate(bid, item, bidder);
    if (validation != null) return validation;
    if (isBuyItNow(item, bid)) return processBuyItNow(bid, item, bidder, after);
    return processRegularBid(bid, item, bidder, after, pendingPriceBroadcast);
  }

  private boolean isBuyItNow(Item item, BidTransaction bid) {
    return item.getMaxPrice() > 0 && bid.getBidValue() >= item.getMaxPrice();
  }

  private Response processBuyItNow(BidTransaction bid, Item item, User bidder, List<Runnable> after) {
    double p = item.getMaxPrice();
    if (!userDao.atomicDeductBalance(bidder.getId(), p)) return BidAuctionValidator.error("Insufficient balance");
    logDao.insertLog(bidder.getId(), "ITEM_BOUGHT", -p, bid.getItemId());
    userDao.addBidderMetrics(bidder.getId(), p);
    if (!itemDao.atomicCloseAuction(item.getId(), bid.getUserId(), "CLOSED")) {
      userDao.atomicCreditBalance(bidder.getId(), p);
      logDao.insertLog(bidder.getId(), "BUY_REFUND", p, bid.getItemId());
      return BidAuctionValidator.error("Auction already closed");
    }
    itemDao.updatePrice(item.getId(), p, item.getVersion());
    creditSeller(item, p, after);
    after.add(() -> notifier.sendBalanceUpdateToUser(bidder.getId()));
    after.add(() -> notifier.broadcastItemClosed(item.getId()));
    final int closedItemId = item.getId();
    after.add(() -> cleanupAutoBidsForItem.accept(closedItemId));
    return new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", bid.getItemId());
  }

  private Response processRegularBid(
      BidTransaction bid, Item item, User bidder, List<Runnable> after, Set<Integer> pendingPriceBroadcast) {
    if (bid.getBidValue() <= item.getCurrentPrice()) return BidAuctionValidator.error("Bid too low");
    if (!userDao.atomicDeductBalance(bidder.getId(), bid.getBidValue()))
      return BidAuctionValidator.error("Insufficient balance");
    logDao.insertLog(bidder.getId(), "BID_HOLD", -bid.getBidValue(), bid.getItemId());
    int prevId = bidDao.getPreviousHighestBidder(bid.getItemId());
    double prevPrice = item.getCurrentPrice();
    if (!bidDao.placeBid(bid)) {
      userDao.atomicCreditBalance(bidder.getId(), bid.getBidValue());
      logDao.insertLog(bidder.getId(), "BID_REFUND", bid.getBidValue(), bid.getItemId());
      return BidAuctionValidator.error("conflict");
    }
    itemDao.updatePrice(item.getId(), bid.getBidValue(), item.getVersion());
    refundPreviousBidder(prevId, prevPrice, bid.getItemId(), after);
    after.add(() -> applyAntiSnipeExtension(bid.getItemId()));
    after.add(() -> notifier.sendBalanceUpdateToUser(bidder.getId()));
    pendingPriceBroadcast.add(bid.getItemId());
    if (prevId > 0) {
      final int outbidItemId = bid.getItemId();
      final int outbidUserId = prevId;
      after.add(() -> notifier.notifyOutbidUser(outbidUserId, outbidItemId));
    }
    return new Response("", Response.OK, "success", bid);
  }

  private void creditSeller(Item item, double amount, List<Runnable> after) {
    userDao.atomicCreditBalance(item.getSellerId(), amount);
    userDao.addSellerMetrics(item.getSellerId(), amount);
    logDao.insertLog(item.getSellerId(), "ITEM_SOLD", amount, item.getId());
    after.add(() -> notifier.sendBalanceUpdateToUser(item.getSellerId()));
  }

  private void refundPreviousBidder(int prevId, double prevPrice, int itemId, List<Runnable> after) {
    if (prevId <= 0 || prevPrice <= 0) return;
    userDao.atomicCreditBalance(prevId, prevPrice);
    logDao.insertLog(prevId, "BID_REFUND", prevPrice, itemId);
    after.add(() -> notifier.sendBalanceUpdateToUser(prevId));
  }

  private void applyAntiSnipeExtension(int itemId) {
    Item item = itemDao.getById(itemId);
    if (item != null
        && item.getEndTime() != null
        && Duration.between(LocalDateTime.now(), item.getEndTime()).getSeconds() < ANTI_SNIPE_THRESHOLD_SECONDS)
      itemDao.updateEndTime(item.getId(), item.getEndTime().plusSeconds(ANTI_SNIPE_THRESHOLD_SECONDS));
  }
}
