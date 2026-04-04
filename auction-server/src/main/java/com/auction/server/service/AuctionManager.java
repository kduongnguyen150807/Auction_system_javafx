package com.auction.server.service;

import com.auction.server.controller.ClientHandler;
import com.auction.server.dao.ItemDao;
import com.auction.shared.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {
  private static AuctionManager instance;
  private List<ClientHandler> clients;
  private BidService bidservice;
  private ItemDao itemdao;

  private AuctionManager() {
    this.clients = new CopyOnWriteArrayList<>();
    this.bidservice = new BidService();
    this.itemdao = new ItemDao();
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
    if (res != null && res.getmaxprice() > 0 && b.getbidvalue() >= res.getmaxprice()) {
      itemdao.updateprice(res.getid(), res.getmaxprice(), res.getversion());
      itemdao.closeauction(res.getid(), b.getuserid(), "CLOSED");
      Response ans = new Response("", Response.ok, "BUY_IT_NOW_SUCCESS", b.getitemid());
      broadcast(ans);
      return ans;
    }

    Response ans = this.bidservice.placebid(b);
    if (ans.getstatus().equals(Response.ok)) {
      broadcast(ans);
    }
    return ans;
  }

  public void broadcast(Response r) {
    for (ClientHandler c : this.clients) {
      c.send(r);
    }
  }
}