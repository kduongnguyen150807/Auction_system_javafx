package com.auction.server.service.auction;

import com.auction.server.controller.ClientHandler;
import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.user.UserDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.LeaderboardEntry;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class AuctionManager {
  private static volatile AuctionManager instance;

  private static final long SESSION_TTL_MILLIS = 24L * 60 * 60 * 1000;

  private final ClientConnectionHub connections = new ClientConnectionHub();
  private final ItemDao itemdao;
  private final UserDao userdao;
  private final BidDao biddao;
  private final TransactionLogDao logdao;
  private final ConcurrentHashMap<Long, ReentrantLock> auctionlocks = new ConcurrentHashMap<>();
  private final BidAuctionValidator bidvalidator = new BidAuctionValidator();
  private final AuctionRealtimeNotifier realtime;
  private final AuctionBidPipeline bidpipeline;
  private final AutoBidCoordinator autobidcoordinator;
  private final BanCascadeService bancascade;
  private final LeaderboardService leaderboardservice = new LeaderboardService();

  private final ConcurrentHashMap<String, SessionInfo> sessions = new ConcurrentHashMap<>();

  private static class SessionInfo {
    private final User user;
    private final long expiresAt;

    private SessionInfo(User user, long expiresAt) {
      this.user = user;
      this.expiresAt = expiresAt;
    }

    private boolean isExpired() {
      return System.currentTimeMillis() > expiresAt;
    }
  }

  private AuctionManager() {
    itemdao = new ItemDao();
    userdao = new UserDao();
    biddao = new BidDao();
    logdao = new TransactionLogDao();
    realtime = new AuctionRealtimeNotifier(itemdao, userdao, connections);
    bidpipeline = new AuctionBidPipeline(itemdao, userdao, biddao, logdao, realtime, bidvalidator);
    autobidcoordinator = new AutoBidCoordinator(itemdao, userdao, biddao, bidvalidator);
    bidpipeline.setCleanupAutoBidsForItem(autobidcoordinator::cleanup);
    bancascade = new BanCascadeService(itemdao, userdao, biddao, logdao, this);
    initleaderboard();
  }

  AuctionManager(ItemDao id, UserDao ud, BidDao bd, TransactionLogDao ld) {
    itemdao = id;
    userdao = ud;
    biddao = bd;
    logdao = ld;
    realtime = new AuctionRealtimeNotifier(itemdao, userdao, connections);
    bidpipeline = new AuctionBidPipeline(itemdao, userdao, biddao, logdao, realtime, bidvalidator);
    autobidcoordinator = new AutoBidCoordinator(itemdao, userdao, biddao, bidvalidator);
    bidpipeline.setCleanupAutoBidsForItem(autobidcoordinator::cleanup);
    bancascade = new BanCascadeService(itemdao, userdao, biddao, logdao, this);
    initleaderboard();
  }

  private void initleaderboard() {
    List<User> res = userdao.getAllUsers();
    for (User ans : res) {
      if (ans.getMoneySpent() > 0 && ans.getRole() != com.auction.shared.UserRole.ADMIN) {
        leaderboardservice.updatescore(
                ans.getId(),
                ans.getUsername(),
                ans.getAvatarUrl(),
                ans.getMoneySpent());
      }
    }
  }

  public void registersession(String token, User user) {
    long expiresAt = System.currentTimeMillis() + SESSION_TTL_MILLIS;
    registersession(token, user, expiresAt);
  }

  public void registersession(String token, User user, long expiresAt) {
    if (token == null || token.isBlank() || user == null) {
      return;
    }

    sessions.put(token, new SessionInfo(user, expiresAt));
  }

  public User getsession(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }

    SessionInfo session = sessions.get(token);

    if (session == null) {
      return null;
    }

    if (session.isExpired()) {
      sessions.remove(token);
      return null;
    }

    return session.user;
  }

  public void removesession(String token) {
    if (token != null) {
      sessions.remove(token);
    }
  }

  public static AuctionManager getInstance() {
    if (instance == null) {
      synchronized (AuctionManager.class) {
        if (instance == null) {
          instance = new AuctionManager();
        }
      }
    }
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
    return auctionlocks.computeIfAbsent(id, k -> new ReentrantLock());
  }

  public void addClient(ClientHandler client) {
    connections.addClient(client);
  }

  public void removeClient(ClientHandler client) {
    connections.removeClient(client);
  }

  public Response processBid(BidTransaction bid) {
    User earlycheck = userdao.getById(String.valueOf(bid.getUserId()));
    if (earlycheck == null) {
      return BidAuctionValidator.error("User not found");
    }

    if (earlycheck.isLocked() || !earlycheck.isActive()) {
      return BidAuctionValidator.error("Account is suspended");
    }

    ReentrantLock lock = getAuctionLock(bid.getItemId());

    try {
      if (!lock.tryLock(500, TimeUnit.MILLISECONDS)) {
        return BidAuctionValidator.error("Auction is busy, please try again");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return BidAuctionValidator.error("Bid interrupted");
    }

    List<Runnable> after = new ArrayList<>();
    Set<Integer> pendingpricebroadcast = new HashSet<>();
    ManualBidExecutor exec = bidpipeline::processManualBid;

    try {
      if (bid.isAutoBid()) {
        return autobidcoordinator.handleRegistration(bid, after, pendingpricebroadcast, exec);
      }

      Response ans = bidpipeline.processManualBid(bid, after, pendingpricebroadcast);

      if (ans != null && Response.OK.equals(ans.getStatus())) {
        autobidcoordinator.runRounds(bid.getItemId(), after, pendingpricebroadcast, exec);
      }

      return ans;
    } finally {
      for (Integer itemid : pendingpricebroadcast) {
        int id = itemid;
        after.add(() -> realtime.broadcastPriceUpdate(id));
      }

      lock.unlock();

      after.forEach(
              task -> {
                try {
                  task.run();
                } catch (Exception e) {
                }
              });
    }
  }

  public void handleBidderBan(int bidderid) {
    bancascade.handleBidderBan(bidderid);
  }

  public void handleSellerBan(int sellerid) {
    bancascade.handleSellerBan(sellerid);
  }

  public boolean voluntarySellerCancelOpenAuction(int sellerId, int itemId) {
    ReentrantLock lock = getAuctionLock(itemId);
    lock.lock();

    try {
      return bancascade.voluntarySellerCancelOpen(itemId, sellerId);
    } catch (SQLException e) {
      return false;
    } finally {
      lock.unlock();
    }
  }

  void cleanupAutoBids(int itemid) {
    autobidcoordinator.cleanup(itemid);
  }

  public void sendBalanceUpdateToUser(int userid) {
    realtime.sendBalanceUpdateToUser(userid);
  }

  void broadcastPriceUpdate(int itemid) {
    realtime.broadcastPriceUpdate(itemid);
  }

  void broadcastItemClosed(int itemid) {
    realtime.broadcastItemClosed(itemid);
  }

  public void sendToUser(int userid, Response response) {
    connections.sendToUser(userid, response);
  }

  public void broadcast(Response response) {
    connections.broadcast(response);
  }
}