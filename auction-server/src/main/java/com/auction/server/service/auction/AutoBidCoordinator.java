package com.auction.server.service.auction;

import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.user.UserDao;
import com.auction.shared.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Registers max auto-bids and runs counter-offer rounds after successful manual bids. */
final class AutoBidCoordinator {

  private static final Logger LOGGER = LoggerFactory.getLogger(AutoBidCoordinator.class);
  private static final int MAX_AUTO_BID_ROUNDS = 50;

  private final ItemDao itemDao;
  private final UserDao userDao;
  private final BidDao bidDao;
  private final BidAuctionValidator validator;
  private final ConcurrentHashMap<Integer, PriorityQueue<AutoBidRegistration>> registry = new ConcurrentHashMap<>();

  AutoBidCoordinator(ItemDao itemDao, UserDao userDao, BidDao bidDao, BidAuctionValidator validator) {
    this.itemDao = itemDao;
    this.userDao = userDao;
    this.bidDao = bidDao;
    this.validator = validator;
  }

  void cleanup(int itemId) {
    registry.remove(itemId);
  }

  Response handleRegistration(
      BidTransaction bid, List<Runnable> after, Set<Integer> pendingPriceBroadcast, ManualBidExecutor executor) {
    Item item = itemDao.getById(bid.getItemId());
    User bidder = userDao.getById(String.valueOf(bid.getUserId()));
    Response v = validator.validate(bid, item, bidder);
    if (v != null) return v;
    if (item.getAuctionType() == AuctionType.DUTCH) {
      return BidAuctionValidator.error("Auto-bid is not available for Dutch auctions");
    }
    if (bid.getMaxAutoBid() <= item.getCurrentPrice())
      return BidAuctionValidator.error("Max auto-bid must be higher than current price");
    PriorityQueue<AutoBidRegistration> regs =
        registry.computeIfAbsent(bid.getItemId(), k -> AutoBidRegistration.newQueue());
    regs.removeIf(r -> r.userId == bid.getUserId());
    double inc = bid.getAutoBidIncrement() > 0 ? bid.getAutoBidIncrement() : 10.0;
    regs.add(new AutoBidRegistration(bid.getUserId(), bid.getMaxAutoBid(), inc, LocalDateTime.now()));
    LOGGER.info("AutoBid registered: user={} item={} max={}", bid.getUserId(), bid.getItemId(), bid.getMaxAutoBid());
    double firstBid = Math.min(item.getCurrentPrice() + inc, bid.getMaxAutoBid());
    if (firstBid <= item.getCurrentPrice()) return BidAuctionValidator.error("Auto-bid ceiling too low for current price");
    BidTransaction init = new BidTransaction(bid.getItemId(), bid.getUserId(), firstBid);
    init.setAutoBid(false);
    init.setTimestamp(LocalDateTime.now());
    Response result = executor.execute(init, after, pendingPriceBroadcast);
    if (result != null && Response.OK.equals(result.getStatus()))
      runRounds(bid.getItemId(), after, pendingPriceBroadcast, executor);
    return result;
  }

  void runRounds(int itemId, List<Runnable> after, Set<Integer> pendingPriceBroadcast, ManualBidExecutor executor) {
    PriorityQueue<AutoBidRegistration> regs = registry.get(itemId);
    if (regs == null || regs.isEmpty()) return;
    for (int round = 0; round < MAX_AUTO_BID_ROUNDS; round++) {
      Item item = itemDao.getById(itemId);
      if (item == null || item.getStatus() != ItemStatus.OPEN) break;
      double currentPrice = item.getCurrentPrice();
      int currentLeader = bidDao.getPreviousHighestBidder(itemId);
      PriorityQueue<AutoBidRegistration> skipped = AutoBidRegistration.newQueue();
      AutoBidRegistration chosen = null;
      double chosenNextBid = 0;
      while (!regs.isEmpty()) {
        AutoBidRegistration reg = regs.poll();
        double nextBid = Math.min(currentPrice + reg.increment, reg.maxAutoBid);
        if (reg.userId == currentLeader || reg.maxAutoBid <= currentPrice || nextBid <= currentPrice) {
          skipped.add(reg);
          continue;
        }
        chosen = reg;
        chosenNextBid = nextBid;
        break;
      }
      regs.addAll(skipped);
      if (chosen == null) break;
      final AutoBidRegistration winner = chosen;
      BidTransaction counterBid = new BidTransaction(itemId, winner.userId, chosenNextBid);
      counterBid.setAutoBid(false);
      counterBid.setTimestamp(LocalDateTime.now());
      Response result = executor.execute(counterBid, after, pendingPriceBroadcast);
      if (result == null || !Response.OK.equals(result.getStatus())) {
        regs.removeIf(r -> r.userId == winner.userId);
        LOGGER.info("AutoBid removed (bid failed): user={}", winner.userId);
      }
    }
    Item item = itemDao.getById(itemId);
    if (item != null) regs.removeIf(r -> r.maxAutoBid <= item.getCurrentPrice());
    else regs.clear();
  }
}
