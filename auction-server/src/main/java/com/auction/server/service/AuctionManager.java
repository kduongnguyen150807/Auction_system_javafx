package com.auction.server.service;

import com.auction.server.controller.ClientHandler;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.TransactionLogDao;
import com.auction.server.dao.UserDao;
import com.auction.shared.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {
  private static AuctionManager instance;
  private List<ClientHandler> clients;
  private BidService bidservice;
  private ItemDao itemDao;
  private UserDao userDao;
  private TransactionLogDao logDao;
  private java.util.Map<Integer, java.util.PriorityQueue<BidTransaction>> autobids;

  private AuctionManager() {
    this.clients = new CopyOnWriteArrayList<>();
    this.bidservice = new BidService();
    this.itemDao = new ItemDao();
    this.userDao = new UserDao();
    this.logDao = new TransactionLogDao();
    this.autobids = new java.util.concurrent.ConcurrentHashMap<>();
  }

  public static synchronized AuctionManager getInstance() {
    if (instance == null) instance = new AuctionManager();
    return instance;
  }

  public void addClient(ClientHandler c) {
    this.clients.add(c);
  }

  public void removeClient(ClientHandler c) {
    this.clients.remove(c);
  }

  public synchronized Response processBid(BidTransaction b) {
    Item res = itemDao.getById(b.getItemId());
    User ans = userDao.getById(String.valueOf(b.getUserId()));

    if (ans != null) {
      String ph = ans.getPhoneNumber();
      if (ph == null || ph.trim().isEmpty())
        return new Response(
            "", Response.ERROR, "Unverified account. Add a phone number to bid.", null);
    }
    if (res != null && res.getSellerId() == b.getUserId())
      return new Response("", Response.ERROR, "Fail", null);

    /* Auto-bid: chỉ đặt mức đấu tối thiểu (giá hiện tại + bước nhảy). Trần maxAutoBid dùng khi có người khác đấu.
    Không add vào queue ở đây — phải sau placeBid OK, tránh queue rỗng / sai khi hết tiền hoặc bid lỗi. */
    if (b.isAutoBid()) {
      b.setBidValue(res.getCurrentPrice() + b.getAutoBidIncrement());
    }

    if (res != null && b.getBidValue() <= res.getCurrentPrice()) {
      b.setBidValue(res.getCurrentPrice() + b.getAutoBidIncrement());
    }

    if (ans != null && ans.getBalance() < b.getBidValue())
      return new Response("", Response.ERROR, "Fail", null);

    if (res != null && res.getMaxPrice() > 0 && b.getBidValue() >= res.getMaxPrice()) {
      double res1 = res.getMaxPrice();
      userDao.updateBalance(ans.getId(), ans.getBalance() - res1);
      userDao.addBidderMetrics(ans.getId(), res1);
      logDao.insertLog(ans.getId(), "ITEM_BOUGHT", -res1, b.getItemId());
      sendToUser(
          ans.getId(),
          new Response(
              "", "BALANCE_UPDATE", "Success", userDao.getById(String.valueOf(ans.getId()))));

      User ans1 = userDao.getById(String.valueOf(res.getSellerId()));
      if (ans1 != null) {
        userDao.updateBalance(ans1.getId(), ans1.getBalance() + res1);
        userDao.addSellerMetrics(ans1.getId(), res1);
        logDao.insertLog(ans1.getId(), "ITEM_SOLD", res1, b.getItemId());
        sendToUser(
            ans1.getId(),
            new Response(
                "", "BALANCE_UPDATE", "Success", userDao.getById(String.valueOf(ans1.getId()))));
      }
      itemDao.updatePrice(res.getId(), res1, res.getVersion());
      itemDao.closeAuction(res.getId(), b.getUserId(), "CLOSED");
      Response ans2 = new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", b.getItemId());
      broadcast(ans2);
      int res4 = getPreviousHighestBidder(b.getItemId());
      if (res4 > 0) {
        sendToUser(res4, new Response("", "OUTBID_NOTIFY", "outbid", b.getItemId()));
      }
      Item res5 = itemDao.getById(b.getItemId());
      if (res5 != null) {
        broadcast(new Response("", "NEW_BID_UPDATE", "priceupdate", res5));
      }
      return ans2;
    }

    int res2 = getPreviousHighestBidder(b.getItemId());
    double res3 = res.getCurrentPrice();
    userDao.updateBalance(ans.getId(), ans.getBalance() - b.getBidValue());
    logDao.insertLog(ans.getId(), "BID_HOLD", -b.getBidValue(), b.getItemId());
    sendToUser(
        ans.getId(),
        new Response(
            "", "BALANCE_UPDATE", "Success", userDao.getById(String.valueOf(ans.getId()))));

    if (res2 > 0 && res3 > 0) {
      User ans3 = userDao.getById(String.valueOf(res2));
      if (ans3 != null) {
        userDao.updateBalance(res2, ans3.getBalance() + res3);
        logDao.insertLog(res2, "BID_REFUND", res3, b.getItemId());
        sendToUser(
            res2,
            new Response("", "BALANCE_UPDATE", "Outbid", userDao.getById(String.valueOf(res2))));
      }
    }

    Response ans4 = this.bidservice.placeBid(b);
    if (ans4.getStatus().equals(Response.OK)) {
      Item res4 = itemDao.getById(b.getItemId());
      if (res4 != null) {
        java.time.LocalDateTime res5 = java.time.LocalDateTime.now();
        java.time.LocalDateTime ans5 = res4.getEndTime();
        if (ans5 != null && java.time.Duration.between(res5, ans5).getSeconds() < 60) {
          java.time.LocalDateTime res6 = ans5.plusSeconds(60);
          itemDao.updateEndTime(res4.getId(), res6);
          res4.setEndTime(res6);
        }
        broadcast(new Response("", "NEW_BID_UPDATE", "priceupdate", res4));
      }
      broadcast(ans4);
      if (res2 > 0) {
        sendToUser(res2, new Response("", "OUTBID_NOTIFY", "outbid", b.getItemId()));
      }
      if (b.isAutoBid()) {
        registerAutoBidQueue(b);
      }
      tryProcessAutoCounters(b.getItemId());
    }

    return ans4;
  }

  /** Ghi nhận trần auto-bid sau khi đặt giá thành công (chỉ lúc này mới có trong hàng đợi). */
  private void registerAutoBidQueue(BidTransaction b) {
    java.util.PriorityQueue<BidTransaction> q =
        this.autobids.computeIfAbsent(
            b.getItemId(),
            k ->
                new java.util.PriorityQueue<>(
                    10,
                    (a1, a2) -> Double.compare(a2.getMaxAutoBid(), a1.getMaxAutoBid())));
    q.add(b);
  }

  /**
   * Sau mỗi lần giá thay đổi thành công: nếu có người đăng ký auto-bid (không phải người đang dẫn) và trần
   * cho phép, đặt giúp một bước (giá hiện tại + increment).
   */
  private void tryProcessAutoCounters(int itemId) {
    java.util.PriorityQueue<BidTransaction> queue = this.autobids.get(itemId);
    if (queue == null || queue.isEmpty()) return;
    double currentPrice = itemDao.getById(itemId).getCurrentPrice();
    int currentLeader = getPreviousHighestBidder(itemId);
    java.util.List<BidTransaction> snapshot = new java.util.ArrayList<>();
    while (!queue.isEmpty()) {
      snapshot.add(queue.poll());
    }
    for (BidTransaction reg : snapshot) {
      if (reg.getUserId() != currentLeader && reg.getMaxAutoBid() > currentPrice) {
        double nextBid = currentPrice + reg.getAutoBidIncrement();
        if (nextBid <= reg.getMaxAutoBid()) {
          BidTransaction counter = new BidTransaction(itemId, reg.getUserId(), nextBid);
          counter.setAutoBid(false);
          counter.setAutoBidIncrement(reg.getAutoBidIncrement());
          processBid(counter);
          break;
        }
      }
    }
    for (BidTransaction reg : snapshot) {
      queue.add(reg);
    }
  }

  private int getPreviousHighestBidder(int id) {
    int ans = -1;
    try {
      java.sql.Connection res =
          com.auction.server.dao.DatabaseConnection.getInstance().getConnection();
      String res1 =
          "SELECT userid FROM bid_transactions WHERE itemid = ? ORDER BY bidvalue DESC LIMIT 1";
      java.sql.PreparedStatement ans1 = res.prepareStatement(res1);
      ans1.setInt(1, id);
      java.sql.ResultSet res2 = ans1.executeQuery();
      if (res2.next()) ans = res2.getInt("userid");
    } catch (Exception e) {
    }
    return ans;
  }

  public void sendToUser(int id, Response r) {
    for (ClientHandler ans : this.clients) {
      if (ans.getCurrentUser() != null && ans.getCurrentUser().getId() == id) ans.send(r);
    }
  }

  public void broadcast(Response r) {
    for (ClientHandler ans : this.clients) ans.send(r);
  }
}
