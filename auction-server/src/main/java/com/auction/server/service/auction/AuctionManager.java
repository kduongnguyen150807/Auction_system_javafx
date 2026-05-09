package com.auction.server.service.auction;

import com.auction.server.controller.ClientHandler;
import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.server.dao.user.UserDao;
import com.auction.shared.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuctionManager.class);
  private static volatile AuctionManager instance;

  private final ClientConnectionHub connections = new ClientConnectionHub();
  private final ItemDao itemDao;
  private final UserDao userDao;
  private final BidDao bidDao;
  private final TransactionLogDao logDao;
  private final ConcurrentHashMap<Long, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();
  private final BidAuctionValidator bidValidator = new BidAuctionValidator();
  private final AuctionRealtimeNotifier realtime;
  private final AuctionBidPipeline bidPipeline;
  private final AutoBidCoordinator autoBidCoordinator;
  private final BanCascadeService banCascade;
  private final LeaderboardService leaderboardservice = new LeaderboardService();

  private AuctionManager() {
    itemDao = new ItemDao();
    userDao = new UserDao();
    bidDao = new BidDao();
    logDao = new TransactionLogDao();
    realtime = new AuctionRealtimeNotifier(itemDao, userDao, connections);
    bidPipeline = new AuctionBidPipeline(itemDao, userDao, bidDao, logDao, realtime, bidValidator);
    autoBidCoordinator = new AutoBidCoordinator(itemDao, userDao, bidDao, bidValidator);
    bidPipeline.setCleanupAutoBidsForItem(autoBidCoordinator::cleanup);
    banCascade = new BanCascadeService(itemDao, userDao, bidDao, logDao, this);
  }

  AuctionManager(ItemDao id, UserDao ud, BidDao bd, TransactionLogDao ld) {
    itemDao = id;
    userDao = ud;
    bidDao = bd;
    logDao = ld;
    realtime = new AuctionRealtimeNotifier(itemDao, userDao, connections);
    bidPipeline = new AuctionBidPipeline(itemDao, userDao, bidDao, logDao, realtime, bidValidator);
    autoBidCoordinator = new AutoBidCoordinator(itemDao, userDao, bidDao, bidValidator);
    bidPipeline.setCleanupAutoBidsForItem(autoBidCoordinator::cleanup);
    banCascade = new BanCascadeService(itemDao, userDao, bidDao, logDao, this);
  }

  public static AuctionManager getInstance() {
    if (instance == null) synchronized (AuctionManager.class) { if (instance == null) instance = new AuctionManager(); }
    return instance;
  }
  public LeaderboardService getLeaderboardservice() {
    return leaderboardservice;
  }

  public void broadcastleaderboard() {
    List<LeaderboardEntry> ans = leaderboardservice.gettop(10);
    Response res = new Response("", "LEADERBOARD_UPDATE", "update", (java.io.Serializable) ans);
    broadcast(res);
  }
  static void resetForTest() {
    instance = null;
  }

  public ReentrantLock getAuctionLock(long id) {
    return auctionLocks.computeIfAbsent(id, k -> new ReentrantLock());
  }

  public void addClient(ClientHandler client) {
    connections.addClient(client);
  }

  public void removeClient(ClientHandler client) {
    connections.removeClient(client);
  }

  public Response processBid(BidTransaction bid) {
    User earlyCheck = userDao.getById(String.valueOf(bid.getUserId()));
    if (earlyCheck == null) return BidAuctionValidator.error("User not found");
    if (earlyCheck.isLocked() || !earlyCheck.isActive()) {
      LOGGER.warn("Rejected bid from banned/locked user={} on item={}", bid.getUserId(), bid.getItemId());
      return BidAuctionValidator.error("Account is suspended");
    }
    ReentrantLock lock = getAuctionLock(bid.getItemId());
    try {
      if (!lock.tryLock(500, TimeUnit.MILLISECONDS)) return BidAuctionValidator.error("Auction is busy, please try again");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return BidAuctionValidator.error("Bid interrupted");
    }
    List<Runnable> after = new ArrayList<>();
    Set<Integer> pendingPriceBroadcast = new HashSet<>();
    ManualBidExecutor exec = bidPipeline::processManualBid;
    try {
      if (bid.isAutoBid()) return autoBidCoordinator.handleRegistration(bid, after, pendingPriceBroadcast, exec);
      Response result = bidPipeline.processManualBid(bid, after, pendingPriceBroadcast);
      if (result != null && Response.OK.equals(result.getStatus()))
        autoBidCoordinator.runRounds(bid.getItemId(), after, pendingPriceBroadcast, exec);
      return result;
    } finally {
      for (Integer itemId : pendingPriceBroadcast) {
        final int id = itemId;
        after.add(() -> realtime.broadcastPriceUpdate(id));
      }
      lock.unlock();
      after.forEach(task -> { try { task.run(); } catch (Exception e) { LOGGER.warn("Post-bid task failed", e); } });
    }
  }

  public void handleBidderBan(int bidderId) {
    banCascade.handleBidderBan(bidderId);
  }

  public void handleSellerBan(int sellerId) {
    banCascade.handleSellerBan(sellerId);
  }

  void cleanupAutoBids(int itemId) {
    autoBidCoordinator.cleanup(itemId);
  }

  public void sendBalanceUpdateToUser(int userId) {
    realtime.sendBalanceUpdateToUser(userId);
  }

  void broadcastPriceUpdate(int itemId) {
    realtime.broadcastPriceUpdate(itemId);
  }

  void broadcastItemClosed(int itemId) {
    realtime.broadcastItemClosed(itemId);
  }

  public void sendToUser(int userId, Response response) {
    connections.sendToUser(userId, response);
  }

  public void broadcast(Response response) {
    connections.broadcast(response);
  }
}
