package com.auction.server.controller;

import com.auction.server.dao.ItemDao;
import com.auction.server.dao.LotDao;
import com.auction.server.dao.UserDao;
import com.auction.server.service.AuctionManager;
import com.auction.server.service.UserService;
import com.auction.shared.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
  private Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;
  private UserService userservice;
  private ItemDao itemdao;
  private LotDao lotdao;

  public ClientHandler(Socket s) {
    this.socket = s;
    this.userservice = new UserService();
    this.itemdao = new ItemDao();
    this.lotdao = new LotDao();
    try {
      this.out = new ObjectOutputStream(this.socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.socket.getInputStream());
      AuctionManager.getinstance().addclient(this);
    } catch (Exception e) {}
  }

  @Override
  public void run() {
    try {
      while (true) {
        Request req = (Request) this.in.readObject();
        Response ans = process(req);
        if (ans != null) {
          this.out.writeObject(ans);
          this.out.flush();
        }
      }
    } catch (EOFException e) {
    } catch (Exception e) {
    } finally {
      AuctionManager.getinstance().removeclient(this);
      try {
        this.socket.close();
      } catch (Exception e) {}
    }
  }

  private Response process(Request req) {
    Response ans = null;
    String act = req.getaction();
    if (act.equals(Request.login)) {
      Map<String, String> data = (Map<String, String>) req.getpayload();
      User u = this.userservice.login(data.get("username"), data.get("password"));
      if (u != null) ans = new Response(req.getrequestid(), Response.ok, "success", u);
      else ans = new Response(req.getrequestid(), Response.err, "fail", null);
    } else if (act.equals(Request.signup)) {
      Map<String, String> data = (Map<String, String>) req.getpayload();
      User u = new Bidder(data.get("username"), data.get("password"), data.get("email"), data.get("age"), "");
      boolean res = this.userservice.signup(u);
      if (res) ans = new Response(req.getrequestid(), Response.ok, "success", null);
      else ans = new Response(req.getrequestid(), Response.err, "duplicate_username_or_email", null);
    } else if (act.equals(Request.list)) {
      List<Item> items = this.itemdao.getall();
      items.removeIf(i -> i.getstatus() != com.auction.shared.ItemStatus.OPEN);
      ans = new Response(req.getrequestid(), Response.ok, "success", items);
    } else if (act.equals(Request.bid)) {
      BidTransaction b = (BidTransaction) req.getpayload();
      Response sysres = AuctionManager.getinstance().processbid(b);
      ans = new Response(req.getrequestid(), sysres.getstatus(), sysres.getmessage(), sysres.getpayload());
    } else if (act.equals(Request.updateprofile)) {
      Map<String, String> data = (Map<String, String>) req.getpayload();
      String err = this.userservice.updateprofile(Integer.parseInt(data.get("userid")), data.get("fullname"), data.get("email"), data.get("phone"));
      if (err == null) ans = new Response(req.getrequestid(), Response.ok, "success", null);
      else ans = new Response(req.getrequestid(), Response.err, err, null);
    } else if (act.equals(Request.updateavatar)) {
      String[] res = ((String) req.getpayload()).split(" ");
      String username = res[0];
      String url = res[1];
      try {
        this.userservice.updateavatar(username, url);
        ans = new Response(req.getrequestid(), Response.ok, "success", null);
      } catch (Exception e) {
        ans = new Response(req.getrequestid(), Response.err, "fail", null);
      }
    } else if (act.equals(Request.getallusers)) {
      java.util.List<User> res = this.userservice.getallusers();
      ans = new Response(req.getrequestid(), Response.ok, "success", res);
    } else if (act.equals(Request.lockuser)) {
      String targetusername = (String) req.getpayload();
      boolean res = this.userservice.setuserlocked(targetusername, true);
      if (res) ans = new Response(req.getrequestid(), Response.ok, "success", null);
      else ans = new Response(req.getrequestid(), Response.err, "fail", null);
    } else if (act.equals(Request.unlockuser)) {
      String targetusername = (String) req.getpayload();
      boolean res = this.userservice.setuserlocked(targetusername, false);
      if (res) ans = new Response(req.getrequestid(), Response.ok, "success", null);
      else ans = new Response(req.getrequestid(), Response.err, "fail", null);
    } else if (act.equals(Request.addlot)) {
      try {
        Map<String, String> data = (Map<String, String>) req.getpayload();
        String title = data.get("name");
        String description = data.get("description");
        double startprice = Double.parseDouble(data.get("startingprice"));
        double maxp = Double.parseDouble(data.getOrDefault("maxprice", "0"));

        java.time.LocalDateTime starttime;
        try {
          starttime = java.time.LocalDateTime.parse(data.get("starttime"), java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        } catch (java.time.format.DateTimeParseException ignored) {
          starttime = java.time.LocalDateTime.parse(data.get("starttime"), java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        java.time.LocalDateTime endtime;
        try {
          endtime = java.time.LocalDateTime.parse(data.get("endtime"), java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        } catch (java.time.format.DateTimeParseException ignored) {
          endtime = java.time.LocalDateTime.parse(data.get("endtime"), java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        String sellerusername = data.get("sellerusername");
        String imageurl = data.getOrDefault("imageurl", "");

        boolean res = this.itemdao.insertlot(title, description, startprice, maxp, starttime, endtime, sellerusername, imageurl, data.getOrDefault("category", "Vehicle"));

        if (res) ans = new Response(req.getrequestid(), Response.ok, "success", null);
        else ans = new Response(req.getrequestid(), Response.err, "fail", null);
      } catch (Exception e) {
        ans = new Response(req.getrequestid(), Response.err, "fail", null);
      }
    } else if (act.equals(Request.getongoingbids)) {
      int res = (int) req.getpayload();
      java.util.List<com.auction.shared.Lot> ans2 = this.lotdao.getongoingbids(res);
      ans = new Response(req.getrequestid(), Response.ok, "success", (java.io.Serializable) ans2);
    } else if (act.equals(Request.getupcomingbids)) {
      int res = (int) req.getpayload();
      java.util.List<com.auction.shared.Lot> ans2 = this.lotdao.getupcomingbids(res);
      ans = new Response(req.getrequestid(), Response.ok, "success", (java.io.Serializable) ans2);
    } else if (act.equals("getclosedbids")) {
      int res = (int) req.getpayload();
      java.util.List<com.auction.shared.Lot> ans2 = this.lotdao.getclosedbids(res);
      ans = new Response(req.getrequestid(), Response.ok, "success", (java.io.Serializable) ans2);
    } else if (act.equals("getpastbids")) {
      int res = (int) req.getpayload();
      java.util.List<com.auction.shared.Lot> ans2 = this.lotdao.getpastbids(res);
      ans = new Response(req.getrequestid(), Response.ok, "success", (java.io.Serializable) ans2);
    } else if (act.equals("deposit")) {
      Map<String, String> data = (Map<String, String>) req.getpayload();
      int id = Integer.parseInt(data.get("userid"));
      double val = Double.parseDouble(data.get("amount"));
      UserDao d = new UserDao();
      User u = d.getbyid(String.valueOf(id));
      if (u != null) {
        boolean res = d.updatebalance(id, u.getbalance() + val);
        if (res) {
          u.setbalance(u.getbalance() + val);
          ans = new Response(req.getrequestid(), Response.ok, "success", u);
        } else {
          ans = new Response(req.getrequestid(), Response.err, "fail", null);
        }
      } else {
        ans = new Response(req.getrequestid(), Response.err, "fail", null);
      }
    } else {
      ans = new Response(req.getrequestid(), Response.err, "unknown", null);
    }
    return ans;
  }

  public void send(Response r) {
    try {
      this.out.writeObject(r);
      this.out.flush();
    } catch (Exception e) {}
  }
}