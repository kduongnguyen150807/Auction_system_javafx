package com.auction.server.service;

import com.auction.server.controller.ClientHandler;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.UserDao;
import com.auction.server.dao.TransactionLogDao;
import com.auction.shared.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {
  private static AuctionManager instance;
  private List<ClientHandler> clients;
  private BidService bidservice;
  private ItemDao itemdao;
  private UserDao userdao;
  private TransactionLogDao logdao;
  private java.util.Map<Integer, java.util.Map<Integer, Double>> autobids;

  private AuctionManager() {
    this.clients = new CopyOnWriteArrayList<>();
    this.bidservice = new BidService();
    this.itemdao = new ItemDao();
    this.userdao = new UserDao();
    this.logdao = new TransactionLogDao();
    this.autobids = new java.util.concurrent.ConcurrentHashMap<>();
  }

  public static synchronized AuctionManager getinstance() {
    if (instance == null) instance = new AuctionManager();
    return instance;
  }

  public void addclient(ClientHandler c) {
    this.clients.add(c);
  }

  public void removeclient(ClientHandler c) {
    this.clients.remove(c);
  }

  public synchronized Response processbid(BidTransaction b) {
    Item res = itemdao.getbyid(b.getitemid());
    User ans = userdao.getbyid(String.valueOf(b.getuserid()));

    if (ans != null) {
      String ph = ans.getphonenumber();
      if (ph == null || ph.trim().isEmpty()) return new Response("", Response.err, "Unverified account. Add a phone number to bid.", null);
    }
    if (res != null && res.getsellerid() == b.getuserid()) return new Response("", Response.err, "Fail", null);

    if (b.getisautobid()) {
      java.util.Map<Integer, Double> res1 = this.autobids.computeIfAbsent(b.getitemid(), k -> new java.util.concurrent.ConcurrentHashMap<>());
      res1.put(b.getuserid(), b.getmaxautobid());
      b.setbidvalue(res.getcurrentprice() + 10.0);
    }

    if (res != null && b.getbidvalue() <= res.getcurrentprice()) {
      b.setbidvalue(res.getcurrentprice() + 10.0);
    }

    if (ans != null && ans.getbalance() < b.getbidvalue()) return new Response("", Response.err, "Fail", null);

    if (res != null && res.getmaxprice() > 0 && b.getbidvalue() >= res.getmaxprice()) {
      double res1 = res.getmaxprice();
      userdao.updatebalance(ans.getid(), ans.getbalance() - res1);
      userdao.addbiddermetrics(ans.getid(), res1);
      logdao.insertlog(ans.getid(), "ITEM_BOUGHT", -res1, b.getitemid());
      sendtouser(ans.getid(), new Response("", "BALANCE_UPDATE", "Success", userdao.getbyid(String.valueOf(ans.getid()))));

      User ans1 = userdao.getbyid(String.valueOf(res.getsellerid()));
      if (ans1 != null) {
        userdao.updatebalance(ans1.getid(), ans1.getbalance() + res1);
        userdao.addsellermetrics(ans1.getid(), res1);
        logdao.insertlog(ans1.getid(), "ITEM_SOLD", res1, b.getitemid());
        sendtouser(ans1.getid(), new Response("", "BALANCE_UPDATE", "Success", userdao.getbyid(String.valueOf(ans1.getid()))));
      }
      itemdao.updateprice(res.getid(), res1, res.getversion());
      itemdao.closeauction(res.getid(), b.getuserid(), "CLOSED");
      Response ans2 = new Response("", Response.ok, "BUY_IT_NOW_SUCCESS", b.getitemid());
      broadcast(ans2);
      int res4 = getprevioushighestbidder(b.getitemid());
      if (res4 > 0) {
        sendtouser(res4, new Response("", "OUTBID_NOTIFY", "outbid", b.getitemid()));
      }
      Item res5 = itemdao.getbyid(b.getitemid());
      if (res5 != null) {
        broadcast(new Response("", "PRICE_UPDATE", "priceupdate", res5));
      }
      return ans2;
    }

    int res2 = getprevioushighestbidder(b.getitemid());
    double res3 = res.getcurrentprice();
    userdao.updatebalance(ans.getid(), ans.getbalance() - b.getbidvalue());
    logdao.insertlog(ans.getid(), "BID_HOLD", -b.getbidvalue(), b.getitemid());
    sendtouser(ans.getid(), new Response("", "BALANCE_UPDATE", "Success", userdao.getbyid(String.valueOf(ans.getid()))));

    if (res2 > 0 && res3 > 0) {
      User ans3 = userdao.getbyid(String.valueOf(res2));
      if (ans3 != null) {
        userdao.updatebalance(res2, ans3.getbalance() + res3);
        logdao.insertlog(res2, "BID_REFUND", res3, b.getitemid());
        sendtouser(res2, new Response("", "BALANCE_UPDATE", "Outbid", userdao.getbyid(String.valueOf(res2))));
      }
    }

    Response ans4 = this.bidservice.placebid(b);
    if (ans4.getstatus().equals(Response.ok)) {
      Item res4 = itemdao.getbyid(b.getitemid());
      if (res4 != null) {
        java.time.LocalDateTime res5 = java.time.LocalDateTime.now();
        java.time.LocalDateTime ans5 = res4.getendtime();
        if (ans5 != null && java.time.Duration.between(res5, ans5).getSeconds() < 60) {
          java.time.LocalDateTime res6 = ans5.plusSeconds(60);
          itemdao.updateendtime(res4.getid(), res6);
          res4.setendtime(res6);
        }
        broadcast(new Response("", "PRICE_UPDATE", "priceupdate", res4));
      }
      broadcast(ans4);
      if (res2 > 0) {
        sendtouser(res2, new Response("", "OUTBID_NOTIFY", "outbid", b.getitemid()));
      }
    }

    java.util.Map<Integer, Double> ans5 = this.autobids.get(b.getitemid());
    if (ans5 != null) {
      double res6 = itemdao.getbyid(b.getitemid()).getcurrentprice();
      int res7 = getprevioushighestbidder(b.getitemid());
      for (java.util.Map.Entry<Integer, Double> res8 : ans5.entrySet()) {
        if (res8.getKey() != res7 && res8.getValue() > res6) {
          double ans6 = res6 + 10.0;
          if (ans6 <= res8.getValue()) {
            BidTransaction ans7 = new BidTransaction(b.getitemid(), res8.getKey(), ans6);
            ans7.setisautobid(false);
            processbid(ans7);
            break;
          }
        }
      }
    }
    return ans4;
  }

  private int getprevioushighestbidder(int id) {
    int ans = -1;
    try {
      java.sql.Connection res = com.auction.server.dao.DatabaseConnection.getinstance().getconnection();
      String res1 = "SELECT userid FROM bid_transactions WHERE itemid = ? ORDER BY bidvalue DESC LIMIT 1";
      java.sql.PreparedStatement ans1 = res.prepareStatement(res1);
      ans1.setInt(1, id);
      java.sql.ResultSet res2 = ans1.executeQuery();
      if (res2.next()) ans = res2.getInt("userid");
    } catch (Exception e) {}
    return ans;
  }

  public void sendtouser(int id, Response r) {
    for (ClientHandler ans : this.clients) {
      if (ans.getcurrentuser() != null && ans.getcurrentuser().getid() == id) ans.send(r);
    }
  }

  public void broadcast(Response r) {
    for (ClientHandler ans : this.clients) ans.send(r);
  }
}