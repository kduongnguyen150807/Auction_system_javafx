package com.auction.server.service;

import com.auction.server.controller.ClientHandler;
import com.auction.server.dao.BidDao;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.TransactionLogDao;
import com.auction.server.dao.UserDao;
import com.auction.shared.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuctionManager.class);
  private static final long ANTI_SNIPE_THRESHOLD_SECONDS = 60;
  private static final int MAX_AUTO_BID_ROUNDS = 50;
  private static volatile AuctionManager instance;

  private final List<ClientHandler> clients;
  private final ItemDao itemDao;
  private final UserDao userDao;
  private final BidDao bidDao;
  private final TransactionLogDao logDao;
  private final ConcurrentHashMap<Long, ReentrantLock> auctionLocks;
  private final ConcurrentHashMap<Integer, PriorityQueue<AutoBidRegistration>> autoBidRegistry;
  private final BanCascadeService banCascade;

  private AuctionManager() {
    clients = new CopyOnWriteArrayList<>(); itemDao = new ItemDao(); userDao = new UserDao();
    bidDao = new BidDao(); logDao = new TransactionLogDao();
    auctionLocks = new ConcurrentHashMap<>(); autoBidRegistry = new ConcurrentHashMap<>();
    banCascade = new BanCascadeService(itemDao, userDao, bidDao, logDao, this);
  }

  AuctionManager(ItemDao id, UserDao ud, BidDao bd, TransactionLogDao ld) {
    clients = new CopyOnWriteArrayList<>(); itemDao = id; userDao = ud; bidDao = bd; logDao = ld;
    auctionLocks = new ConcurrentHashMap<>(); autoBidRegistry = new ConcurrentHashMap<>();
    banCascade = new BanCascadeService(itemDao, userDao, bidDao, logDao, this);
  }

  public static AuctionManager getInstance() {
    if (instance == null) synchronized (AuctionManager.class) { if (instance == null) instance = new AuctionManager(); }
    return instance;
  }

  static void resetForTest() { instance = null; }

  public ReentrantLock getAuctionLock(long id) { return auctionLocks.computeIfAbsent(id, k -> new ReentrantLock()); }
  public void addClient(ClientHandler client) { clients.add(client); }
  public void removeClient(ClientHandler client) { clients.remove(client); }

  public Response processBid(BidTransaction bid) {
    User earlyCheck = userDao.getById(String.valueOf(bid.getUserId()));
    if (earlyCheck == null) return err("User not found");
    if (earlyCheck.isLocked() || !earlyCheck.isActive()) {
      LOGGER.warn("Rejected bid from banned/locked user={} on item={}", bid.getUserId(), bid.getItemId());
      return err("Account is suspended");
    }
    ReentrantLock lock = getAuctionLock(bid.getItemId());
    try {
      if (!lock.tryLock(500, TimeUnit.MILLISECONDS)) return err("Auction is busy, please try again");
    } catch (InterruptedException e) { Thread.currentThread().interrupt(); return err("Bid interrupted"); }
    List<Runnable> after = new ArrayList<>();
    try {
      if (bid.isAutoBid()) return handleAutoBidRegistration(bid, after);
      Response result = doProcessManualBid(bid, after);
      if (result != null && Response.OK.equals(result.getStatus())) runAutoBidRounds(bid.getItemId(), after);
      return result;
    } finally {
      lock.unlock();
      after.forEach(task -> { try { task.run(); } catch (Exception e) { LOGGER.warn("Post-bid task failed", e); } });
    }
  }

  public void handleBidderBan(int bidderId) { banCascade.handleBidderBan(bidderId); }
  public void handleSellerBan(int sellerId) { banCascade.handleSellerBan(sellerId); }

  private Response handleAutoBidRegistration(BidTransaction bid, List<Runnable> after) {
    Item item = itemDao.getById(bid.getItemId());
    User bidder = userDao.getById(String.valueOf(bid.getUserId()));
    Response v = validateBid(bid, item, bidder);
    if (v != null) return v;
    if (bid.getMaxAutoBid() <= item.getCurrentPrice())
      return err("Max auto-bid must be higher than current price");
    PriorityQueue<AutoBidRegistration> regs = autoBidRegistry.computeIfAbsent(bid.getItemId(), k -> AutoBidRegistration.newQueue());
    regs.removeIf(r -> r.userId == bid.getUserId());
    double inc = bid.getAutoBidIncrement() > 0 ? bid.getAutoBidIncrement() : 10.0;
    regs.add(new AutoBidRegistration(bid.getUserId(), bid.getMaxAutoBid(), inc, LocalDateTime.now()));
    LOGGER.info("AutoBid registered: user={} item={} max={}", bid.getUserId(), bid.getItemId(), bid.getMaxAutoBid());
    double firstBid = Math.min(item.getCurrentPrice() + inc, bid.getMaxAutoBid());
    if (firstBid <= item.getCurrentPrice()) return err("Auto-bid ceiling too low for current price");
    BidTransaction init = new BidTransaction(bid.getItemId(), bid.getUserId(), firstBid);
    init.setAutoBid(false); init.setTimestamp(LocalDateTime.now());
    Response result = doProcessManualBid(init, after);
    if (result != null && Response.OK.equals(result.getStatus())) runAutoBidRounds(bid.getItemId(), after);
    return result;
  }

  private Response doProcessManualBid(BidTransaction bid, List<Runnable> after) {
    Item item = itemDao.getById(bid.getItemId());
    User bidder = userDao.getById(String.valueOf(bid.getUserId()));
    Response validation = validateBid(bid, item, bidder);
    if (validation != null) return validation;
    if (isBuyItNow(item, bid)) return processBuyItNow(bid, item, bidder, after);
    return processRegularBid(bid, item, bidder, after);
  }

  private Response validateBid(BidTransaction bid, Item item, User bidder) {
    if (item == null) return err("Item not found");
    if (item.getStatus() != ItemStatus.OPEN) return err("Auction is no longer open");
    if (item.getEndTime() != null && item.getEndTime().isBefore(LocalDateTime.now())) return err("Auction has ended");
    if (bidder == null) return err("User not found");
    if (bidder.isLocked() || !bidder.isActive()) return err("Account is locked");
    String phone = bidder.getPhoneNumber();
    if (phone == null || phone.trim().isEmpty()) return err("Unverified account. Add a phone number to bid.");
    if (item.getSellerId() == bid.getUserId()) return err("Cannot bid on your own item");
    return null;
  }
  private Response err(String msg) { return new Response("", Response.ERROR, msg, null); }

  private boolean isBuyItNow(Item item, BidTransaction bid) {
    return item.getMaxPrice() > 0 && bid.getBidValue() >= item.getMaxPrice();
  }

  private Response processBuyItNow(BidTransaction bid, Item item, User bidder, List<Runnable> after) {
    double p = item.getMaxPrice();
    if (!userDao.atomicDeductBalance(bidder.getId(), p)) return err("Insufficient balance");
    logDao.insertLog(bidder.getId(), "ITEM_BOUGHT", -p, bid.getItemId());
    userDao.addBidderMetrics(bidder.getId(), p);
    if (!itemDao.atomicCloseAuction(item.getId(), bid.getUserId(), "CLOSED")) {
      userDao.atomicCreditBalance(bidder.getId(), p); logDao.insertLog(bidder.getId(), "BUY_REFUND", p, bid.getItemId());
      return err("Auction already closed");
    }
    itemDao.updatePrice(item.getId(), p, item.getVersion()); creditSeller(item, p, after);
    after.add(() -> sendBalanceUpdateToUser(bidder.getId()));
    after.add(() -> broadcastItemClosed(item.getId()));
    after.add(() -> cleanupAutoBids(item.getId()));
    return new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", bid.getItemId());
  }

  private Response processRegularBid(BidTransaction bid, Item item, User bidder, List<Runnable> after) {
    if (bid.getBidValue() <= item.getCurrentPrice()) return err("Bid too low");
    if (!userDao.atomicDeductBalance(bidder.getId(), bid.getBidValue())) return err("Insufficient balance");
    logDao.insertLog(bidder.getId(), "BID_HOLD", -bid.getBidValue(), bid.getItemId());
    int prevId = bidDao.getPreviousHighestBidder(bid.getItemId());
    double prevPrice = item.getCurrentPrice();
    if (!bidDao.placeBid(bid)) {
      userDao.atomicCreditBalance(bidder.getId(), bid.getBidValue());
      logDao.insertLog(bidder.getId(), "BID_REFUND", bid.getBidValue(), bid.getItemId());
      return err("conflict");
    }
    itemDao.updatePrice(item.getId(), bid.getBidValue(), item.getVersion());
    refundPreviousBidder(prevId, prevPrice, bid.getItemId(), after);
    after.add(() -> applyAntiSnipeExtension(bid.getItemId()));
    after.add(() -> sendBalanceUpdateToUser(bidder.getId()));
    after.add(() -> broadcastPriceUpdate(bid.getItemId()));
    if (prevId > 0) after.add(() -> sendToUser(prevId, new Response("", "OUTBID_NOTIFY", "outbid", bid.getItemId())));
    return new Response("", Response.OK, "success", bid);
  }

  private void runAutoBidRounds(int itemId, List<Runnable> after) {
    PriorityQueue<AutoBidRegistration> regs = autoBidRegistry.get(itemId);
    if (regs == null || regs.isEmpty()) return;
    for (int round = 0; round < MAX_AUTO_BID_ROUNDS; round++) {
      Item item = itemDao.getById(itemId);
      if (item == null || item.getStatus() != ItemStatus.OPEN) break;
      double currentPrice = item.getCurrentPrice();
      int currentLeader = bidDao.getPreviousHighestBidder(itemId);
      PriorityQueue<AutoBidRegistration> skipped = AutoBidRegistration.newQueue();
      AutoBidRegistration chosen = null; double chosenNextBid = 0;
      while (!regs.isEmpty()) {
        AutoBidRegistration reg = regs.poll();
        double nextBid = Math.min(currentPrice + reg.increment, reg.maxAutoBid);
        if (reg.userId == currentLeader || reg.maxAutoBid <= currentPrice || nextBid <= currentPrice) { skipped.add(reg); continue; }
        chosen = reg; chosenNextBid = nextBid; break;
      }
      regs.addAll(skipped);
      if (chosen == null) break;
      final AutoBidRegistration winner = chosen;
      BidTransaction counterBid = new BidTransaction(itemId, winner.userId, chosenNextBid);
      counterBid.setAutoBid(false); counterBid.setTimestamp(LocalDateTime.now());
      Response result = doProcessManualBid(counterBid, after);
      if (result == null || !Response.OK.equals(result.getStatus())) {
        regs.removeIf(r -> r.userId == winner.userId);
        LOGGER.info("AutoBid removed (bid failed): user={}", winner.userId);
      }
    }
    Item item = itemDao.getById(itemId);
    if (item != null) regs.removeIf(r -> r.maxAutoBid <= item.getCurrentPrice()); else regs.clear();
  }

  void cleanupAutoBids(int itemId) { autoBidRegistry.remove(itemId); }

  void sendBalanceUpdateToUser(int userId) {
    User u = userDao.getById(String.valueOf(userId));
    if (u != null) sendToUser(userId, new Response("", "BALANCE_UPDATE", "Success", u));
  }

  private void creditSeller(Item item, double amount, List<Runnable> after) {
    userDao.atomicCreditBalance(item.getSellerId(), amount);
    userDao.addSellerMetrics(item.getSellerId(), amount);
    logDao.insertLog(item.getSellerId(), "ITEM_SOLD", amount, item.getId());
    after.add(() -> sendBalanceUpdateToUser(item.getSellerId()));
  }

  private void refundPreviousBidder(int prevId, double prevPrice, int itemId, List<Runnable> after) {
    if (prevId <= 0 || prevPrice <= 0) return;
    userDao.atomicCreditBalance(prevId, prevPrice);
    logDao.insertLog(prevId, "BID_REFUND", prevPrice, itemId);
    after.add(() -> sendBalanceUpdateToUser(prevId));
  }

  private void applyAntiSnipeExtension(int itemId) {
    Item item = itemDao.getById(itemId);
    if (item != null && item.getEndTime() != null
        && Duration.between(LocalDateTime.now(), item.getEndTime()).getSeconds() < ANTI_SNIPE_THRESHOLD_SECONDS)
      itemDao.updateEndTime(item.getId(), item.getEndTime().plusSeconds(ANTI_SNIPE_THRESHOLD_SECONDS));
  }

  void broadcastPriceUpdate(int itemId) {
    Item item = itemDao.getById(itemId);
    if (item == null) return;
    broadcast(new Response("", "NEW_BID_UPDATE", "priceupdate", item));
    sendToUser(item.getSellerId(), new Response("", "SELLER_BID_NOTIFY", "new_bid_on_your_item", item));
  }

  void broadcastItemClosed(int itemId) {
    Item item = itemDao.getById(itemId);
    if (item != null) broadcast(new Response("", "ITEM_CLOSED", "closed", item));
  }

  public void sendToUser(int userId, Response response) {
    clients.stream().filter(c -> c.getCurrentUser() != null && c.getCurrentUser().getId() == userId)
        .forEach(c -> c.send(response));
  }

  public void broadcast(Response response) { clients.forEach(c -> c.send(response)); }

  private static class AutoBidRegistration {
    final int userId; final double maxAutoBid, increment; final LocalDateTime registrationTime;
    AutoBidRegistration(int userId, double maxAutoBid, double increment, LocalDateTime registrationTime) {
      this.userId = userId; this.maxAutoBid = maxAutoBid; this.increment = increment; this.registrationTime = registrationTime;
    }
    static PriorityQueue<AutoBidRegistration> newQueue() {
      return new PriorityQueue<>(Comparator.comparingDouble((AutoBidRegistration r) -> -r.maxAutoBid)
          .thenComparing(r -> r.registrationTime));
    }
  }
}
