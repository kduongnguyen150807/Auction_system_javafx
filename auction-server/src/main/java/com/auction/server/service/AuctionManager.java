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

    if (b.isAutoBid()) {
      java.util.PriorityQueue<BidTransaction> res1 = this.autobids.get(b.getItemId());
      if (res1 == null) {
        res1 =
            new java.util.PriorityQueue<>(
                10,
                new java.util.Comparator<BidTransaction>() {
                  public int compare(BidTransaction ans1, BidTransaction ans2) {
                    return Double.compare(ans2.getMaxAutoBid(), ans1.getMaxAutoBid());
                  }
                });
        this.autobids.put(b.getItemId(), res1);
      }
      res1.add(b);
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
    }

    java.util.PriorityQueue<BidTransaction> ans5 = this.autobids.get(b.getItemId());
    if (ans5 != null && !ans5.isEmpty()) {
      double res6 = itemDao.getById(b.getItemId()).getCurrentPrice();
      int res7 = getPreviousHighestBidder(b.getItemId());
      java.util.List<BidTransaction> res8 = new java.util.ArrayList<>();
      while (!ans5.isEmpty()) {
        res8.add(ans5.poll());
      }
      for (BidTransaction ans7 : res8) {
        if (ans7.getUserId() != res7 && ans7.getMaxAutoBid() > res6) {
          double res9 = res6 + ans7.getAutoBidIncrement();
          if (res9 <= ans7.getMaxAutoBid()) {
            BidTransaction ans8 = new BidTransaction(b.getItemId(), ans7.getUserId(), res9);
            ans8.setAutoBid(false);
            ans8.setAutoBidIncrement(ans7.getAutoBidIncrement());
            processBid(ans8);
            break;
          }
        }
      }
      for (BidTransaction ans7 : res8) {
        ans5.add(ans7);
      }
    }
    return ans4;
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
