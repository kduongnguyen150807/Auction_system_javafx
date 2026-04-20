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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuctionManager {
  private static final Logger LOGGER = Logger.getLogger(AuctionManager.class.getName());
  private static final long ANTI_SNIPE_THRESHOLD_SECONDS = 60;
  private static final int MAX_AUTO_BID_ROUNDS = 50;

  private static volatile AuctionManager instance;
  private final List<ClientHandler> clients;
  private final ItemDao itemDao;
  private final UserDao userDao;
  private final BidDao bidDao;
  private final TransactionLogDao logDao;
  private final ConcurrentHashMap<Integer, ReentrantLock> itemLocks;

  /**
   * Per-item list of active auto-bid registrations. Each entry represents
   * a user's standing instruction to auto-bid up to their maxAutoBid ceiling.
   */
  private final ConcurrentHashMap<Integer, List<AutoBidRegistration>> autoBidRegistry;

  private AuctionManager() {
    this.clients = new CopyOnWriteArrayList<>();
    this.itemDao = new ItemDao();
    this.userDao = new UserDao();
    this.bidDao = new BidDao();
    this.logDao = new TransactionLogDao();
    this.itemLocks = new ConcurrentHashMap<>();
    this.autoBidRegistry = new ConcurrentHashMap<>();
  }

  public static AuctionManager getInstance() {
    if (instance == null) {
      synchronized (AuctionManager.class) {
        if (instance == null) instance = new AuctionManager();
      }
    }
    return instance;
  }

  private ReentrantLock getLockForItem(int itemId) {
    return this.itemLocks.computeIfAbsent(itemId, k -> new ReentrantLock(true));
  }

  public void addClient(ClientHandler client) {
    this.clients.add(client);
  }

  public void removeClient(ClientHandler client) {
    this.clients.remove(client);
  }

  // ─── Public entry point ────────────────────────────────────────────

  public Response processBid(BidTransaction bid) {
    ReentrantLock lock = getLockForItem(bid.getItemId());
    lock.lock();
    try {
      if (bid.isAutoBid()) {
        return handleAutoBidRegistration(bid);
      }
      Response result = doProcessManualBid(bid);
      if (result != null && Response.OK.equals(result.getStatus())) {
        runAutoBidRounds(bid.getItemId());
      }
      return result;
    } finally {
      lock.unlock();
    }
  }

  // ─── Auto-bid registration ────────────────────────────────────────

  private Response handleAutoBidRegistration(BidTransaction bid) {
    Item item = itemDao.getById(bid.getItemId());
    User bidder = userDao.getById(String.valueOf(bid.getUserId()));

    Response validation = validateBid(bid, item, bidder);
    if (validation != null) return validation;

    if (bid.getMaxAutoBid() <= item.getCurrentPrice()) {
      return new Response("", Response.ERROR,
          "Max auto-bid must be higher than current price", null);
    }

    List<AutoBidRegistration> regs = autoBidRegistry.computeIfAbsent(
        bid.getItemId(), k -> new ArrayList<>());

    regs.removeIf(r -> r.userId == bid.getUserId());

    double increment = bid.getAutoBidIncrement();
    if (increment <= 0) increment = 10.0;

    regs.add(new AutoBidRegistration(
        bid.getUserId(), bid.getMaxAutoBid(), increment));
    LOGGER.info("AutoBid registered: user=" + bid.getUserId()
        + " item=" + bid.getItemId() + " max=" + bid.getMaxAutoBid());

    double firstBid = item.getCurrentPrice() + increment;
    if (firstBid > bid.getMaxAutoBid()) {
      firstBid = bid.getMaxAutoBid();
    }
    if (firstBid <= item.getCurrentPrice()) {
      return new Response("", Response.ERROR, "Auto-bid ceiling too low for current price", null);
    }

    BidTransaction initialBid = new BidTransaction(
        bid.getItemId(), bid.getUserId(), firstBid);
    initialBid.setAutoBid(false);
    initialBid.setTimestamp(LocalDateTime.now());

    Response result = doProcessManualBid(initialBid);

    if (result != null && Response.OK.equals(result.getStatus())) {
      runAutoBidRounds(bid.getItemId());
    }

    return result;
  }

  // ─── Core bid processing (manual / counter bids) ──────────────────

  private Response doProcessManualBid(BidTransaction bid) {
    Item item = itemDao.getById(bid.getItemId());
    User bidder = userDao.getById(String.valueOf(bid.getUserId()));

    Response validation = validateBid(bid, item, bidder);
    if (validation != null) return validation;

    if (isBuyItNow(item, bid)) {
      return processBuyItNow(bid, item, bidder);
    }

    return processRegularBid(bid, item, bidder);
  }

  private Response validateBid(BidTransaction bid, Item item, User bidder) {
    if (item == null) {
      return new Response("", Response.ERROR, "Item not found", null);
    }
    if (item.getStatus() != ItemStatus.OPEN) {
      return new Response("", Response.ERROR, "Auction is no longer open", null);
    }
    if (item.getEndTime() != null && item.getEndTime().isBefore(LocalDateTime.now())) {
      return new Response("", Response.ERROR, "Auction has ended", null);
    }
    if (bidder == null) {
      return new Response("", Response.ERROR, "User not found", null);
    }
    String phone = bidder.getPhoneNumber();
    if (phone == null || phone.trim().isEmpty()) {
      return new Response("", Response.ERROR,
          "Unverified account. Add a phone number to bid.", null);
    }
    if (item.getSellerId() == bid.getUserId()) {
      return new Response("", Response.ERROR, "Cannot bid on your own item", null);
    }
    return null;
  }

  private boolean isBuyItNow(Item item, BidTransaction bid) {
    return item.getMaxPrice() > 0 && bid.getBidValue() >= item.getMaxPrice();
  }

  private Response processBuyItNow(BidTransaction bid, Item item, User bidder) {
    double buyPrice = item.getMaxPrice();

    if (!userDao.atomicDeductBalance(bidder.getId(), buyPrice)) {
      return new Response("", Response.ERROR, "Insufficient balance", null);
    }
    logDao.insertLog(bidder.getId(), "ITEM_BOUGHT", -buyPrice, bid.getItemId());
    userDao.addBidderMetrics(bidder.getId(), buyPrice);

    boolean closed = itemDao.atomicCloseAuction(item.getId(), bid.getUserId(), "CLOSED");
    if (!closed) {
      userDao.atomicCreditBalance(bidder.getId(), buyPrice);
      logDao.insertLog(bidder.getId(), "BUY_REFUND", buyPrice, bid.getItemId());
      return new Response("", Response.ERROR, "Auction already closed", null);
    }

    itemDao.updatePrice(item.getId(), buyPrice, item.getVersion());
    creditSeller(item, buyPrice);

    sendBalanceUpdateToUser(bidder.getId());
    broadcastItemClosed(item.getId());
    cleanupAutoBids(item.getId());

    return new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", bid.getItemId());
  }

  private Response processRegularBid(BidTransaction bid, Item item, User bidder) {
    if (bid.getBidValue() <= item.getCurrentPrice()) {
      return new Response("", Response.ERROR, "Bid too low", null);
    }

    if (!userDao.atomicDeductBalance(bidder.getId(), bid.getBidValue())) {
      return new Response("", Response.ERROR, "Insufficient balance", null);
    }
    logDao.insertLog(bidder.getId(), "BID_HOLD", -bid.getBidValue(), bid.getItemId());

    int previousBidderId = bidDao.getPreviousHighestBidder(bid.getItemId());
    double previousPrice = item.getCurrentPrice();

    boolean bidPlaced = bidDao.placeBid(bid);
    if (!bidPlaced) {
      userDao.atomicCreditBalance(bidder.getId(), bid.getBidValue());
      logDao.insertLog(bidder.getId(), "BID_REFUND", bid.getBidValue(), bid.getItemId());
      return new Response("", Response.ERROR, "conflict", null);
    }

    itemDao.updatePrice(item.getId(), bid.getBidValue(), item.getVersion());
    refundPreviousBidder(previousBidderId, previousPrice, bid.getItemId());

    applyAntiSnipeExtension(bid.getItemId());
    sendBalanceUpdateToUser(bidder.getId());
    broadcastPriceUpdate(bid.getItemId());

    if (previousBidderId > 0) {
      sendToUser(previousBidderId,
          new Response("", "OUTBID_NOTIFY", "outbid", bid.getItemId()));
    }

    return new Response("", Response.OK, "success", bid);
  }

  // ─── Auto-bid counter rounds (iterative, no recursion) ────────────

  private void runAutoBidRounds(int itemId) {
    List<AutoBidRegistration> regs = autoBidRegistry.get(itemId);
    if (regs == null || regs.isEmpty()) return;

    for (int round = 0; round < MAX_AUTO_BID_ROUNDS; round++) {
      Item item = itemDao.getById(itemId);
      if (item == null || item.getStatus() != ItemStatus.OPEN) break;

      double currentPrice = item.getCurrentPrice();
      int currentLeader = bidDao.getPreviousHighestBidder(itemId);

      AutoBidRegistration best = null;
      double bestNextBid = 0;

      for (AutoBidRegistration reg : regs) {
        if (reg.userId == currentLeader) continue;
        if (reg.maxAutoBid <= currentPrice) continue;

        double nextBid = currentPrice + reg.increment;
        if (nextBid > reg.maxAutoBid) nextBid = reg.maxAutoBid;
        if (nextBid <= currentPrice) continue;

        if (best == null || nextBid > bestNextBid
            || (nextBid == bestNextBid && reg.maxAutoBid > best.maxAutoBid)) {
          best = reg;
          bestNextBid = nextBid;
        }
      }

      if (best == null) break;

      final AutoBidRegistration chosen = best;
      BidTransaction counterBid = new BidTransaction(itemId, chosen.userId, bestNextBid);
      counterBid.setAutoBid(false);
      counterBid.setTimestamp(LocalDateTime.now());

      Response result = doProcessManualBid(counterBid);
      if (result == null || !Response.OK.equals(result.getStatus())) {
        regs.removeIf(r -> r.userId == chosen.userId);
        LOGGER.info("AutoBid removed (bid failed): user=" + chosen.userId);
        continue;
      }
    }

    pruneExhaustedRegistrations(itemId);
  }

  private void pruneExhaustedRegistrations(int itemId) {
    List<AutoBidRegistration> regs = autoBidRegistry.get(itemId);
    if (regs == null) return;

    Item item = itemDao.getById(itemId);
    if (item == null) {
      regs.clear();
      return;
    }
    double currentPrice = item.getCurrentPrice();
    regs.removeIf(r -> r.maxAutoBid <= currentPrice);
  }

  private void cleanupAutoBids(int itemId) {
    autoBidRegistry.remove(itemId);
  }

  // ─── Balance / notification helpers ────────────────────────────────

  private void sendBalanceUpdateToUser(int userId) {
    User freshUser = userDao.getById(String.valueOf(userId));
    if (freshUser != null) {
      sendToUser(userId, new Response("", "BALANCE_UPDATE", "Success", freshUser));
    }
  }

  private void creditSeller(Item item, double amount) {
    userDao.atomicCreditBalance(item.getSellerId(), amount);
    userDao.addSellerMetrics(item.getSellerId(), amount);
    logDao.insertLog(item.getSellerId(), "ITEM_SOLD", amount, item.getId());
    sendBalanceUpdateToUser(item.getSellerId());
  }

  private void refundPreviousBidder(int previousBidderId, double previousPrice, int itemId) {
    if (previousBidderId <= 0 || previousPrice <= 0) return;
    userDao.atomicCreditBalance(previousBidderId, previousPrice);
    logDao.insertLog(previousBidderId, "BID_REFUND", previousPrice, itemId);
    sendBalanceUpdateToUser(previousBidderId);
  }

  private void applyAntiSnipeExtension(int itemId) {
    Item item = itemDao.getById(itemId);
    if (item == null || item.getEndTime() == null) return;
    LocalDateTime now = LocalDateTime.now();
    if (Duration.between(now, item.getEndTime()).getSeconds() < ANTI_SNIPE_THRESHOLD_SECONDS) {
      itemDao.updateEndTime(item.getId(),
          item.getEndTime().plusSeconds(ANTI_SNIPE_THRESHOLD_SECONDS));
    }
  }

  private void broadcastPriceUpdate(int itemId) {
    Item updatedItem = itemDao.getById(itemId);
    if (updatedItem != null) {
      broadcast(new Response("", "NEW_BID_UPDATE", "priceupdate", updatedItem));
      sendToUser(updatedItem.getSellerId(),
          new Response("", "SELLER_BID_NOTIFY", "new_bid_on_your_item", updatedItem));
    }
  }

  private void broadcastItemClosed(int itemId) {
    Item closedItem = itemDao.getById(itemId);
    if (closedItem != null) {
      broadcast(new Response("", "ITEM_CLOSED", "closed", closedItem));
    }
  }

  public void sendToUser(int userId, Response response) {
    for (ClientHandler client : this.clients) {
      if (client.getCurrentUser() != null && client.getCurrentUser().getId() == userId) {
        client.send(response);
      }
    }
  }

  public void broadcast(Response response) {
    for (ClientHandler client : this.clients) {
      client.send(response);
    }
  }

  // ─── Inner class for auto-bid registration ────────────────────────

  private static class AutoBidRegistration {
    final int userId;
    final double maxAutoBid;
    final double increment;

    AutoBidRegistration(int userId, double maxAutoBid, double increment) {
      this.userId = userId;
      this.maxAutoBid = maxAutoBid;
      this.increment = increment;
    }
  }
}
