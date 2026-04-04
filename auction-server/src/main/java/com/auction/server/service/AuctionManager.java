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

  private AuctionManager() {
    this.clients = new CopyOnWriteArrayList<>();
    this.bidservice = new BidService();
    this.itemdao = new ItemDao();
    this.userdao = new UserDao();
    this.logdao = new TransactionLogDao();
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

    if (res != null && res.getsellerid() == b.getuserid()) return new Response("", Response.err, "Fail", null);
    if (res != null && b.getbidvalue() <= res.getcurrentprice()) return new Response("", Response.err, "Fail", null);
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
      broadcast(ans4);
      if (res2 > 0) {
        sendtouser(res2, new Response("", "OUTBID_NOTIFY", "outbid", b.getitemid()));
      }
      Item res4 = itemdao.getbyid(b.getitemid());
      if (res4 != null) {
        broadcast(new Response("", "PRICE_UPDATE", "priceupdate", res4));
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