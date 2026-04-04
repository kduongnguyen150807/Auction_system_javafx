package com.auction.server.controller;

import com.auction.server.dao.ItemDao;
import com.auction.server.dao.LotDao;
import com.auction.server.dao.RatingDao;
import com.auction.server.dao.TransactionLogDao;
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
  private TransactionLogDao logdao;
  private RatingDao ratingdao;
  private User currentuser;

  public ClientHandler(Socket s) {
    this.socket = s;
    this.userservice = new UserService();
    this.itemdao = new ItemDao();
    this.lotdao = new LotDao();
    this.logdao = new TransactionLogDao();
    this.ratingdao = new RatingDao();
    try {
      this.out = new ObjectOutputStream(this.socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.socket.getInputStream());
      AuctionManager.getinstance().addclient(this);
    } catch (Exception e) {}
  }

  public User getcurrentuser() {
    User ans = this.currentuser;
    return ans;
  }

  @Override
  public void run() {
    try {
      while (true) {
        Request req = (Request) this.in.readObject();
        Response ans = process(req);
        if (ans != null) {
          synchronized (this.out) {
            this.out.reset();
            this.out.writeObject(ans);
            this.out.flush();
          }
        }
      }
    } catch (EOFException e) {
    } catch (Exception e) {
    } finally {
      AuctionManager.getinstance().removeclient(this);
      try { this.socket.close(); } catch (Exception e) {}
    }
  }

  private Response process(Request req) {
    Response ans = null;
    String act = req.getaction();
    Object pay = req.getpayload();
    String rid = req.getrequestid();

    // 1. NHÓM AUTH (ĐĂNG NHẬP / ĐĂNG KÝ)
    if (act.equals(Request.login)) {
      Map<String, String> res = (Map<String, String>) pay;
      User u = this.userservice.login(res.get("username"), res.get("password"));
      if (u != null) {
        this.currentuser = u;
        ans = new Response(rid, Response.ok, "success", u);
      } else ans = new Response(rid, Response.err, "fail", null);
    }
    else if (act.equals(Request.signup)) {
      Map<String, String> res = (Map<String, String>) pay;
      User u = new Bidder(res.get("username"), res.get("password"), res.get("email"), res.get("age"), "");
      boolean res2 = this.userservice.signup(u);
      ans = new Response(rid, res2 ? Response.ok : Response.err, res2 ? "success" : "duplicate", null);
    }

    // 2. NHÓM ITEM & ĐẤU GIÁ
    else if (act.equals(Request.list)) {
      List<Item> res = this.itemdao.getall();
      res.removeIf(i -> i.getstatus() != com.auction.shared.ItemStatus.OPEN);
      ans = new Response(rid, Response.ok, "success", res);
    }
    else if (act.equals(Request.bid)) {
      BidTransaction res = (BidTransaction) pay;
      Response res2 = AuctionManager.getinstance().processbid(res);
      ans = new Response(rid, res2.getstatus(), res2.getmessage(), res2.getpayload());
    }
    else if (act.equals(Request.addlot)) {
      ans = handleAddLot(req);
    }
    else if (act.equals("get_my_items")) { // Cho trang Your Item
      int res = (int) pay;
      java.util.List<Item> ans2 = this.itemdao.getbysellerid(res);
      ans = new Response(rid, Response.ok, "success", (java.io.Serializable) ans2);
    }

    // 3. NHÓM PROFILE & USER MANAGEMENT
    else if (act.equals(Request.updateprofile)) {
      Map<String, String> res = (Map<String, String>) pay;
      String err = this.userservice.updateprofile(Integer.parseInt(res.get("userid")), res.get("fullname"), res.get("email"), res.get("phone"));
      ans = new Response(rid, err == null ? Response.ok : Response.err, err == null ? "success" : err, null);
    }
    else if (act.equals(Request.updateavatar)) {
      String[] res = ((String) pay).split(" ");
      try {
        this.userservice.updateavatar(res[0], res[1]);
        ans = new Response(rid, Response.ok, "success", null);
      } catch (Exception e) { ans = new Response(rid, Response.err, "fail", null); }
    }
    else if (act.equals(Request.getallusers)) {
      ans = new Response(rid, Response.ok, "success", this.userservice.getallusers());
    }
    else if (act.equals(Request.lockuser) || act.equals(Request.unlockuser)) {
      boolean lock = act.equals(Request.lockuser);
      boolean res = this.userservice.setuserlocked((String) pay, lock);
      ans = new Response(rid, res ? Response.ok : Response.err, res ? "success" : "fail", null);
    }
    else if (act.equals(Request.promoteadmin)) {
      if (this.currentuser != null && this.currentuser.getrole() == UserRole.ADMIN) {
        String res = (String) pay;
        String[] res2 = res.split(":");
        String res3 = res2[0];
        String res4 = res2.length > 1 ? res2[1] : UserRole.ADMIN.name();
        boolean res5 = this.userservice.setuserrole(res3, res4);
        ans = new Response(rid, res5 ? Response.ok : Response.err, res5 ? "success" : "fail", null);
      } else {
        ans = new Response(rid, Response.err, "forbidden", null);
      }
    }

    // 4. NHÓM LỊCH SỬ (BIDDER)
    else if (act.equals(Request.getongoingbids)) {
      ans = new Response(rid, Response.ok, "success", (java.io.Serializable) this.lotdao.getongoingbids((int) pay));
    }
    else if (act.equals(Request.getupcomingbids)) {
      ans = new Response(rid, Response.ok, "success", (java.io.Serializable) this.lotdao.getupcomingbids((int) pay));
    }
    else if (act.equals("getclosedbids")) {
      ans = new Response(rid, Response.ok, "success", (java.io.Serializable) this.lotdao.getclosedbids((int) pay));
    }
    else if (act.equals("getpastbids")) {
      ans = new Response(rid, Response.ok, "success", (java.io.Serializable) this.lotdao.getpastbids((int) pay));
    }

    // 5. NHÓM TÀI CHÍNH & REFRESH
    else if (act.equals("deposit")) {
      ans = handleDeposit(req);
    }
    else if (act.equals("refresh_user")) {
      User u = new com.auction.server.dao.UserDao().getbyid(String.valueOf(pay));
      ans = new Response(rid, u != null ? Response.ok : Response.err, u != null ? "success" : "fail", u);
    }
    else if (act.equals("get_transactions")) {
      ans = new Response(rid, Response.ok, "success", (java.io.Serializable) this.logdao.getbyuserid((int) pay));
    }
    else if (act.equals(Request.getitembyid)) {
      int res = (int) pay;
      Item res2 = this.itemdao.getbyid(res);
      ans = new Response(rid, res2 != null ? Response.ok : Response.err, res2 != null ? "success" : "not_found", res2);
    }
    else if (act.equals(Request.submitrating)) {
      ans = handlesubmitrating(req);
    }
    else if (act.equals(Request.getratings)) {
      int res = (int) pay;
      java.util.List<Rating> res2 = this.ratingdao.getbyitemid(res);
      ans = new Response(rid, Response.ok, "success", (java.io.Serializable) res2);
    }
    else if (act.equals(Request.getpendingitems)) {
      if (this.currentuser != null && this.currentuser.getrole() == UserRole.ADMIN) {
        java.util.List<Item> res = this.itemdao.getpendingitems();
        ans = new Response(rid, Response.ok, "success", (java.io.Serializable) res);
      } else {
        ans = new Response(rid, Response.err, "forbidden", null);
      }
    }
    else if (act.equals(Request.approveitem)) {
      if (this.currentuser != null && this.currentuser.getrole() == UserRole.ADMIN) {
        int res = (int) pay;
        boolean res2 = this.itemdao.approveitem(res);
        ans = new Response(rid, res2 ? Response.ok : Response.err, res2 ? "success" : "fail", null);
      } else {
        ans = new Response(rid, Response.err, "forbidden", null);
      }
    }
    else if (act.equals(Request.rejectitem)) {
      if (this.currentuser != null && this.currentuser.getrole() == UserRole.ADMIN) {
        int res = (int) pay;
        boolean res2 = this.itemdao.rejectitem(res);
        ans = new Response(rid, res2 ? Response.ok : Response.err, res2 ? "success" : "fail", null);
      } else {
        ans = new Response(rid, Response.err, "forbidden", null);
      }
    }
    else {
      ans = new Response(rid, Response.err, "unknown_action", null);
    }
    return ans;
  }

  private Response handlesubmitrating(Request req) {
    String rid = req.getrequestid();
    try {
      Rating res = (Rating) req.getpayload();
      if (this.currentuser == null) return new Response(rid, Response.err, "not_logged_in", null);
      Item res1 = this.itemdao.getbyid(res.getitemid());
      if (res1 == null) return new Response(rid, Response.err, "item_not_found", null);
      if (res1.getstatus() != ItemStatus.CLOSED && res1.getstatus() != ItemStatus.FINISHED)
        return new Response(rid, Response.err, "auction_not_ended", null);
      int res2 = this.currentuser.getid();
      if (res2 != res1.getwinnerid() && res2 != res1.getsellerid())
        return new Response(rid, Response.err, "not_participant", null);
      if (this.ratingdao.hasrated(res.getitemid(), res2))
        return new Response(rid, Response.err, "already_rated", null);
      res.setrateruserid(res2);
      if (res2 == res1.getwinnerid()) {
        res.setrateduserid(res1.getsellerid());
      } else {
        res.setrateduserid(res1.getwinnerid());
      }
      boolean res3 = this.ratingdao.insertrating(res);
      if (res3) {
        this.ratingdao.recalcuserrating(res.getrateduserid());
        return new Response(rid, Response.ok, "success", null);
      }
      return new Response(rid, Response.err, "fail", null);
    } catch (Exception e) {
      return new Response(rid, Response.err, "fail", null);
    }
  }

  private Response handleAddLot(Request req) {
    try {
      Map<String, String> res = (Map<String, String>) req.getpayload();
      java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm[:ss]");
      java.time.LocalDateTime st = java.time.LocalDateTime.parse(res.get("starttime").length() == 16 ? res.get("starttime") + ":00" : res.get("starttime"), f);
      java.time.LocalDateTime et = java.time.LocalDateTime.parse(res.get("endtime").length() == 16 ? res.get("endtime") + ":00" : res.get("endtime"), f);

      boolean ok = this.itemdao.insertlot(res.get("name"), res.get("description"), Double.parseDouble(res.get("startingprice")),
              Double.parseDouble(res.getOrDefault("maxprice", "0")), st, et, res.get("sellerusername"),
              res.getOrDefault("imageurl", ""), res.getOrDefault("category", "Vehicle"));
      return new Response(req.getrequestid(), ok ? Response.ok : Response.err, ok ? "success" : "fail", null);
    } catch (Exception e) { return new Response(req.getrequestid(), Response.err, "fail", null); }
  }

  private Response handleDeposit(Request req) {
    Map<String, String> res = (Map<String, String>) req.getpayload();
    int id = Integer.parseInt(res.get("userid"));
    double val = Double.parseDouble(res.get("amount"));
    com.auction.server.dao.UserDao d = new com.auction.server.dao.UserDao();
    User u = d.getbyid(String.valueOf(id));
    if (u != null && d.updatebalance(id, u.getbalance() + val)) {
      this.logdao.insertlog(id, "DEPOSIT", val, 0);
      u.setbalance(u.getbalance() + val);
      return new Response(req.getrequestid(), Response.ok, "success", u);
    }
    return new Response(req.getrequestid(), Response.err, "fail", null);
  }

  public void send(Response r) {
    try {
      synchronized (this.out) {
        this.out.reset();
        this.out.writeObject(r);
        this.out.flush();
      }
    } catch (Exception e) {}
  }
}