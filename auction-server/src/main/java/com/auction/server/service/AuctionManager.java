package com.auction.server.service;

import com.auction.server.controller.ClientHandler;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.UserDao;
import com.auction.shared.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {
  private static AuctionManager instance;
  private List<ClientHandler> clients;
  private BidService bidservice;
  private ItemDao itemdao;
  private UserDao userdao;

  private AuctionManager() {
    this.clients = new CopyOnWriteArrayList<>();
    this.bidservice = new BidService();
    this.itemdao = new ItemDao();
    this.userdao = new UserDao();
  }

  public static synchronized AuctionManager getinstance() {
    if (instance == null) {
      instance = new AuctionManager();
    }
    AuctionManager ans = instance;
    return ans;
  }

  public void addclient(ClientHandler c) {
    this.clients.add(c);
  }

  public void removeclient(ClientHandler c) {
    this.clients.remove(c);
  }

  public synchronized Response processbid(BidTransaction b) {
    Item res = itemdao.getbyid(b.getitemid());
    User u = userdao.getbyid(String.valueOf(b.getuserid()));

    if (res != null && res.getsellerid() == b.getuserid()) {
      return new Response("", Response.err, "Mày đéo thể tự mua đồ của chính mày được!", null);
    }

    if (res != null && b.getbidvalue() <= res.getcurrentprice()) {
      return new Response("", Response.err, "Giá bid m đưa ra phải lớn hơn giá hiện tại!", null);
    }

    if (u != null && u.getbalance() < b.getbidvalue()) {
      return new Response("", Response.err, "Ví đéo đủ tiền, nạp thêm đi cu!", null);
    }

    if (res != null && res.getmaxprice() > 0 && b.getbidvalue() >= res.getmaxprice()) {
      userdao.updatebalance(u.getid(), u.getbalance() - res.getmaxprice());
      userdao.addbiddermetrics(u.getid(), res.getmaxprice());

      User s = userdao.getbyid(String.valueOf(res.getsellerid()));
      if (s != null) {
        userdao.updatebalance(s.getid(), s.getbalance() + res.getmaxprice());
        userdao.addsellermetrics(s.getid(), res.getmaxprice());
      }

      itemdao.updateprice(res.getid(), res.getmaxprice(), res.getversion());
      itemdao.closeauction(res.getid(), b.getuserid(), "CLOSED");
      Response ans = new Response("", Response.ok, "BUY_IT_NOW_SUCCESS", b.getitemid());
      broadcast(ans);
      return ans;
    }

    int prevbidder = getprevioushighestbidder(b.getitemid());
    double prevprice = res.getcurrentprice();

    userdao.updatebalance(u.getid(), u.getbalance() - b.getbidvalue());

    if (prevbidder > 0 && prevprice > 0) {
      User oldu = userdao.getbyid(String.valueOf(prevbidder));
      if (oldu != null) {
        userdao.updatebalance(prevbidder, oldu.getbalance() + prevprice);
      }
    }

    Response ans = this.bidservice.placebid(b);
    if (ans.getstatus().equals(Response.ok)) {
      broadcast(ans);
    } else {
      userdao.updatebalance(u.getid(), u.getbalance() + b.getbidvalue());
      if (prevbidder > 0 && prevprice > 0) {
        User oldu = userdao.getbyid(String.valueOf(prevbidder));
        if (oldu != null) {
          userdao.updatebalance(prevbidder, oldu.getbalance() - prevprice);
        }
      }
    }
    return ans;
  }

  private int getprevioushighestbidder(int itemid) {
    int ans = -1;
    try {
      java.sql.Connection conn = com.auction.server.dao.DatabaseConnection.getinstance().getconnection();
      String sql = "SELECT userid FROM bid_transactions WHERE itemid = ? ORDER BY bidvalue DESC LIMIT 1";
      java.sql.PreparedStatement ps = conn.prepareStatement(sql);
      ps.setInt(1, itemid);
      java.sql.ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        ans = rs.getInt("userid");
      }
    } catch (Exception e) {}
    return ans;
  }

  public void broadcast(Response r) {
    for (ClientHandler c : this.clients) {
      c.send(r);
    }
  }
}