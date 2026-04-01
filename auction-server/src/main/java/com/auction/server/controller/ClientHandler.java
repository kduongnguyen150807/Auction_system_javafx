package com.auction.server.controller;

import com.auction.server.dao.ItemDao;
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

  public ClientHandler(Socket s) {
    this.socket = s;
    this.userservice = new UserService();
    this.itemdao = new ItemDao();
    try {
      this.out = new ObjectOutputStream(this.socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.socket.getInputStream());
      AuctionManager.getinstance().addclient(this);
    } catch (Exception e) {
      e.printStackTrace();
    }
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
      e.printStackTrace();
    } finally {
      AuctionManager.getinstance().removeclient(this);
      try {
        this.socket.close();
      } catch (Exception e) {
      }
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
      User u =
          new Bidder(
              data.get("username"), data.get("password"), data.get("email"), data.get("age"), "");
      boolean res = this.userservice.signup(u);
      if (res) ans = new Response(req.getrequestid(), Response.ok, "success", null);
      else ans = new Response(req.getrequestid(), Response.err, "duplicate_username_or_email", null);
    } else if (act.equals(Request.list)) {
      List<Item> items = this.itemdao.getall();
      ans = new Response(req.getrequestid(), Response.ok, "success", items);
    } else if (act.equals(Request.bid)) {
      BidTransaction b = (BidTransaction) req.getpayload();
      Response sysres = AuctionManager.getinstance().processbid(b);
      ans =
          new Response(
              req.getrequestid(), sysres.getstatus(), sysres.getmessage(), sysres.getpayload());
    } else if (act.equals(Request.updateprofile)) {
      Map<String, String> data = (Map<String, String>) req.getpayload();
      boolean res = this.userservice.updateprofile(
          Integer.parseInt(data.get("userid")),
          data.get("fullname"),
          data.get("email"),
          data.get("phone"));
      if (res) ans = new Response(req.getrequestid(), Response.ok, "success", null);
      else ans = new Response(req.getrequestid(), Response.err, "fail", null);
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
        java.time.LocalDateTime endtime =
            java.time.LocalDateTime.parse(
                data.get("endtime"), java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String sellerusername = data.get("sellerusername");
        String imageurl = data.getOrDefault("imageurl", "");
        boolean res =
            this.itemdao.insertlot(
                title,
                description,
                startprice,
                endtime,
                sellerusername,
                imageurl,
                data.getOrDefault("category", "Vehicle"));
        if (res) ans = new Response(req.getrequestid(), Response.ok, "success", null);
        else ans = new Response(req.getrequestid(), Response.err, "fail", null);
      } catch (Exception e) {
        e.printStackTrace();
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
    } catch (Exception e) {
    }
  }
}
