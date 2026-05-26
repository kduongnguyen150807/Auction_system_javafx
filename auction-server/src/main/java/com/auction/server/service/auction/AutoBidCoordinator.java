package com.auction.server.service.auction;

import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.user.UserDao;
import com.auction.shared.AuctionType;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class AutoBidCoordinator {
  private final ItemDao itemdao;
  private final UserDao userdao;
  private final BidDao biddao;
  private final BidAuctionValidator validator;
  private final ConcurrentHashMap<Integer, PriorityQueue<AutoBidRegistration>> registry = new ConcurrentHashMap<>();

  AutoBidCoordinator(ItemDao itemdao, UserDao userdao, BidDao biddao, BidAuctionValidator validator) {
    this.itemdao = itemdao;
    this.userdao = userdao;
    this.biddao = biddao;
    this.validator = validator;
  }

  void cleanup(int itemid) {
    registry.remove(itemid);
  }

  Response handleRegistration(BidTransaction bid, List<Runnable> after, Set<Integer> pendingpricebroadcast, ManualBidExecutor executor) {
    Item item = itemdao.getById(bid.getItemId());
    User bidder = userdao.getById(String.valueOf(bid.getUserId()));
    Response ans = validator.validate(bid, item, bidder);
    if (ans != null) {
      return ans;
    }
    if (item.getAuctionType() == AuctionType.DUTCH) {
      return BidAuctionValidator.error("error");
    }
    if (bid.getMaxAutoBid() <= item.getCurrentPrice()) {
      return BidAuctionValidator.error("error");
    }
    PriorityQueue<AutoBidRegistration> regs = registry.computeIfAbsent(bid.getItemId(), k -> AutoBidRegistration.newQueue());
    regs.removeIf(r -> r.userId == bid.getUserId());
    double inc = bid.getAutoBidIncrement() > 0 ? bid.getAutoBidIncrement() : 10.0;
    regs.add(new AutoBidRegistration(bid.getUserId(), bid.getMaxAutoBid(), inc, LocalDateTime.now()));
    double firstbid = Math.min(item.getCurrentPrice() + inc, bid.getMaxAutoBid());
    if (firstbid <= item.getCurrentPrice()) {
      return BidAuctionValidator.error("error");
    }
    BidTransaction init = new BidTransaction(bid.getItemId(), bid.getUserId(), firstbid);
    init.setAutoBid(false);
    init.setTimestamp(LocalDateTime.now());
    Response res = executor.execute(init, after, pendingpricebroadcast);
    if (res != null && Response.OK.equals(res.getStatus())) {
      runRounds(bid.getItemId(), after, pendingpricebroadcast, executor);
    }
    return res;
  }

  void runRounds(int itemid, List<Runnable> after, Set<Integer> pendingpricebroadcast, ManualBidExecutor executor) {
    PriorityQueue<AutoBidRegistration> regs = registry.get(itemid);
    if (regs == null || regs.isEmpty()) {
      return;
    }
    Item item = itemdao.getById(itemid);
    if (item == null || item.getStatus() != ItemStatus.OPEN) {
      return;
    }
    AutoBidRegistration top = regs.peek();
    if (top == null) {
      pruneExhaustedRegistrations(itemid, regs);
      return;
    }
    int currentLeader = biddao.getPreviousHighestBidder(itemid);
    if (top.userId != currentLeader) {
      AutoBidRegistration runnerUp = regs.size() >= 2 ? findRunnerUp(regs, top.userId) : null;
      double target = computeCounterTarget(item.getCurrentPrice(), top, runnerUp);
      if (target > item.getCurrentPrice()) {
        BidTransaction counterbid = new BidTransaction(itemid, top.userId, target);
        counterbid.setAutoBid(false);
        counterbid.setTimestamp(LocalDateTime.now());
        executor.execute(counterbid, after, pendingpricebroadcast);
      }
    }
    pruneExhaustedRegistrations(itemid, regs);
  }

  /**
   * Counter bid for the top proxy: beat current price by increment, and when two proxies compete also
   * respect the second-highest ceiling formula.
   */
  static double computeCounterTarget(
      double currentPrice, AutoBidRegistration top, AutoBidRegistration runnerUp) {
    double beatCurrent = Math.min(currentPrice + top.increment, top.maxAutoBid);
    if (runnerUp == null) {
      return beatCurrent;
    }
    double proxyTarget = Math.min(runnerUp.maxAutoBid + top.increment, top.maxAutoBid);
    if (proxyTarget > currentPrice) {
      return proxyTarget;
    }
    return beatCurrent;
  }

  private static AutoBidRegistration findRunnerUp(
      PriorityQueue<AutoBidRegistration> regs, int topUserId) {
    AutoBidRegistration runnerUp = null;
    for (AutoBidRegistration candidate : regs) {
      if (candidate.userId == topUserId) {
        continue;
      }
      if (runnerUp == null
          || candidate.maxAutoBid > runnerUp.maxAutoBid
          || (candidate.maxAutoBid == runnerUp.maxAutoBid
              && candidate.registrationTime.isBefore(runnerUp.registrationTime))) {
        runnerUp = candidate;
      }
    }
    return runnerUp;
  }

  private void pruneExhaustedRegistrations(int itemid, PriorityQueue<AutoBidRegistration> regs) {
    Item freshitem = itemdao.getById(itemid);
    if (freshitem != null) {
      regs.removeIf(r -> r.maxAutoBid <= freshitem.getCurrentPrice());
    } else {
      registry.remove(itemid);
    }
  }
}