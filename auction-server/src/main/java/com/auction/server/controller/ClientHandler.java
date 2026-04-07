package com.auction.server.controller;

import com.auction.server.dao.ItemDao;
import com.auction.server.dao.LotDao;
import com.auction.server.dao.RatingDao;
import com.auction.server.dao.TransactionLogDao;
import com.auction.server.service.AuctionManager;
import com.auction.server.service.UserService;
import com.auction.server.service.WorldChatService;
import com.auction.shared.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
  private Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;
  private UserService userService;
  private ItemDao itemDao;
  private LotDao lotDao;
  private TransactionLogDao logDao;
  private RatingDao ratingDao;
  private User currentUser;

  public ClientHandler(Socket s) {
    this.socket = s;
    this.userService = new UserService();
    this.itemDao = new ItemDao();
    this.lotDao = new LotDao();
    this.logDao = new TransactionLogDao();
    this.ratingDao = new RatingDao();
    try {
      this.out = new ObjectOutputStream(this.socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.socket.getInputStream());
      AuctionManager.getInstance().addClient(this);
      WorldChatService.getInstance().addClient(this);
    } catch (Exception e) {
    }
  }

  public User getCurrentUser() {
    User ans = this.currentUser;
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
      AuctionManager.getInstance().removeClient(this);
      try {
        this.socket.close();
      } catch (Exception e) {
      }
    }
  }

  private Response process(Request req) {
    Response ans = null;
    String act = req.getAction();
    Object pay = req.getPayload();
    String rid = req.getRequestId();

    if (act.equals(Request.LOGIN)) {
      Map<String, String> res = (Map<String, String>) pay;
      User u = this.userService.login(res.get("username"), res.get("password"));
      if (u != null) {
        this.currentUser = u;
        ans = new Response(rid, Response.OK, "success", u);
      } else ans = new Response(rid, Response.ERROR, "fail", null);
    } else if (act.equals(Request.SIGNUP)) {
      try {
        User res = (User) pay;
        boolean res2 = this.userService.signup(res);
        ans = new Response(rid, res2 ? Response.OK : Response.ERROR, res2 ? "success" : "duplicate", null);
      } catch (ClassCastException e) {
        e.printStackTrace();
        ans = new Response(rid, Response.ERROR, "class_cast_error", null);
      } catch (Exception e) {
        e.printStackTrace();
        ans = new Response(rid, Response.ERROR, "server_error", null);
      }
    } else if (act.equals(Request.LIST) || act.equals(Request.GET_ONGOING_LOTS)) {
      List<Item> res = this.itemDao.getAll();
      res.removeIf(i -> i.getStatus() != com.auction.shared.ItemStatus.OPEN);
      ans = new Response(rid, Response.OK, "success", res);
    } else if (act.equals(Request.BID)) {
      BidTransaction res = (BidTransaction) pay;
      Response res2 = AuctionManager.getInstance().processBid(res);
      ans = new Response(rid, res2.getStatus(), res2.getMessage(), res2.getPayload());
    } else if (act.equals(Request.ADD_LOT)) {
      ans = handleAddLot(req);
    } else if (act.equals("get_my_items")) {
      int res = (int) pay;
      java.util.List<Item> ans2 = this.itemDao.getBySellerId(res);
      ans = new Response(rid, Response.OK, "success", (java.io.Serializable) ans2);
    } else if (act.equals(Request.UPDATE_PROFILE)) {
      Map<String, String> res = (Map<String, String>) pay;
      String err =
              this.userService.updateProfile(
                      Integer.parseInt(res.get("userid")),
                      res.get("fullname"),
                      res.get("email"),
                      res.get("phone"));
      ans =
              new Response(
                      rid, err == null ? Response.OK : Response.ERROR, err == null ? "success" : err, null);
    } else if (act.equals(Request.UPDATE_AVATAR)) {
      String[] res = ((String) pay).split(" ");
      try {
        this.userService.updateAvatar(res[0], res[1]);
        ans = new Response(rid, Response.OK, "success", null);
      } catch (Exception e) {
        ans = new Response(rid, Response.ERROR, "fail", null);
      }
    } else if (act.equals(Request.GET_ALL_USERS)) {
      ans = new Response(rid, Response.OK, "success", this.userService.getAllUsers());
    } else if (act.equals(Request.LOCK_USER) || act.equals(Request.UNLOCK_USER)) {
      boolean lock = act.equals(Request.LOCK_USER);
      boolean res = this.userService.setUserLocked((String) pay, lock);
      ans = new Response(rid, res ? Response.OK : Response.ERROR, res ? "success" : "fail", null);
    } else if (act.equals(Request.PROMOTE_ADMIN)) {
      if (this.currentUser != null && this.currentUser.getRole() == UserRole.ADMIN) {
        String res = (String) pay;
        String[] res2 = res.split(":");
        String res3 = res2[0];
        String res4 = res2.length > 1 ? res2[1] : UserRole.ADMIN.name();
        boolean res5 = this.userService.setUserRole(res3, res4);
        ans =
                new Response(rid, res5 ? Response.OK : Response.ERROR, res5 ? "success" : "fail", null);
      } else {
        ans = new Response(rid, Response.ERROR, "forbidden", null);
      }
    } else if (act.equals(Request.GET_ONGOING_BIDS)) {
      ans =
              new Response(
                      rid,
                      Response.OK,
                      "success",
                      (java.io.Serializable) this.lotDao.getOngoingBids((int) pay));
    } else if (act.equals(Request.GET_UPCOMING_BIDS)) {
      ans =
              new Response(
                      rid,
                      Response.OK,
                      "success",
                      (java.io.Serializable) this.lotDao.getUpcomingBids((int) pay));
    } else if (act.equals("getclosedbids")) {
      ans =
              new Response(
                      rid,
                      Response.OK,
                      "success",
                      (java.io.Serializable) this.lotDao.getClosedBids((int) pay));
    } else if (act.equals("getpastbids")) {
      ans =
              new Response(
                      rid,
                      Response.OK,
                      "success",
                      (java.io.Serializable) this.lotDao.getPastBids((int) pay));
    } else if (act.equals("deposit")) {
      ans = handleDeposit(req);
    } else if (act.equals("refresh_user")) {
      User u = new com.auction.server.dao.UserDao().getById(String.valueOf(pay));
      ans =
              new Response(
                      rid, u != null ? Response.OK : Response.ERROR, u != null ? "success" : "fail", u);
    } else if (act.equals("get_transactions")) {
      ans =
              new Response(
                      rid,
                      Response.OK,
                      "success",
                      (java.io.Serializable) this.logDao.getByUserId((int) pay));
    } else if (act.equals(Request.GET_ITEM_BY_ID)) {
      int res = (int) pay;
      Item res2 = this.itemDao.getById(res);
      ans =
              new Response(
                      rid,
                      res2 != null ? Response.OK : Response.ERROR,
                      res2 != null ? "success" : "not_found",
                      res2);
    } else if (act.equals(Request.SUBMIT_RATING)) {
      ans = handleSubmitRating(req);
    } else if (act.equals(Request.GET_RATINGS)) {
      int res = (int) pay;
      java.util.List<Rating> res2 = this.ratingDao.getByItemId(res);
      ans = new Response(rid, Response.OK, "success", (java.io.Serializable) res2);
    } else if (act.equals(Request.GET_PENDING_ITEMS)) {
      if (this.currentUser != null && this.currentUser.getRole() == UserRole.ADMIN) {
        java.util.List<Item> res = this.itemDao.getPendingItems();
        ans = new Response(rid, Response.OK, "success", (java.io.Serializable) res);
      } else {
        ans = new Response(rid, Response.ERROR, "forbidden", null);
      }
    } else if (act.equals(Request.APPROVE_ITEM)) {
      if (this.currentUser != null && this.currentUser.getRole() == UserRole.ADMIN) {
        int res = (int) pay;
        boolean res2 = this.itemDao.approveItem(res);
        ans =
                new Response(rid, res2 ? Response.OK : Response.ERROR, res2 ? "success" : "fail", null);
      } else {
        ans = new Response(rid, Response.ERROR, "forbidden", null);
      }
    } else if (act.equals(Request.REJECT_ITEM)) {
      if (this.currentUser != null && this.currentUser.getRole() == UserRole.ADMIN) {
        int res = (int) pay;
        boolean res2 = this.itemDao.rejectItem(res);
        ans =
                new Response(rid, res2 ? Response.OK : Response.ERROR, res2 ? "success" : "fail", null);
      } else {
        ans = new Response(rid, Response.ERROR, "forbidden", null);
      }
    } else if (act.equals(Request.SEARCH_USERS)) {
      String res = (String) pay;
      java.util.List<User> res2 = new com.auction.server.dao.UserDao().searchUsers(res);
      ans = new Response(rid, Response.OK, "success", (java.io.Serializable) res2);
    } else if (act.equals(Request.GET_USER_BY_ID)) {
      int res = (int) pay;
      User u = new com.auction.server.dao.UserDao().getById(String.valueOf(res));
      if (u != null) u.setPassword("");
      ans =
              new Response(
                      rid,
                      u != null ? Response.OK : Response.ERROR,
                      u != null ? "success" : "not_found",
                      u);
    } else if (act.equals("get_status_stats")) {
      if (this.currentUser != null && this.currentUser.getRole() == UserRole.ADMIN) {
        ans = new Response(rid, Response.OK, "success", this.itemDao.getStatusStats());
      } else ans = new Response(rid, Response.ERROR, "forbidden", null);
    } else if (act.equals("get_category_stats")) {
      if (this.currentUser != null && this.currentUser.getRole() == UserRole.ADMIN) {
        ans = new Response(rid, Response.OK, "success", this.itemDao.getCategoryStats());
      } else ans = new Response(rid, Response.ERROR, "forbidden", null);
    } else if (act.equals("get_bid_history")) {
      int res = (int) pay;
      java.util.List<BidTransaction> ans2 = new java.util.ArrayList<>();
      try {
        java.sql.Connection res1 =
                com.auction.server.dao.DatabaseConnection.getInstance().getConnection();
        java.sql.PreparedStatement ans1 =
                res1.prepareStatement(
                        "SELECT * FROM bid_transactions WHERE itemid = ? ORDER BY timestamp ASC");
        ans1.setInt(1, res);
        java.sql.ResultSet res2 = ans1.executeQuery();
        while (res2.next()) {
          BidTransaction ans3 = new BidTransaction();
          ans3.setBidValue(res2.getDouble("bidvalue"));
          java.sql.Timestamp res3 = res2.getTimestamp("timestamp");
          if (res3 != null) ans3.setTimestamp(res3.toLocalDateTime());
          ans2.add(ans3);
        }
      } catch (Exception e) {
      }
      ans = new Response(rid, Response.OK, "success", (java.io.Serializable) ans2);
    } else if (act.equals("ping")) {
      ans = new Response(rid, Response.OK, "pong", null);
    } else if (act.equals(Request.SEND_MESSAGE)) {
      WorldChatService.getInstance().enqueueMessage(req);
      System.out.println("processing message");
    } else if(act.equals(Request.GET_CHAT_HISTORY)){
      List<HashMap<String, String>> history = WorldChatService.getInstance().getChatHistory();
      ans = new Response(rid, Response.OK, "success", history);
    }
    else {
      ans = new Response(rid, Response.ERROR, "unknown_action", null);
    }
    return ans;
  }

  private Response handleSubmitRating(Request req) {
    String rid = req.getRequestId();
    try {
      Rating res = (Rating) req.getPayload();
      if (this.currentUser == null) return new Response(rid, Response.ERROR, "not_logged_in", null);
      Item res1 = this.itemDao.getById(res.getItemId());
      if (res1 == null) return new Response(rid, Response.ERROR, "item_not_found", null);
      if (res1.getStatus() != ItemStatus.CLOSED && res1.getStatus() != ItemStatus.FINISHED)
        return new Response(rid, Response.ERROR, "auction_not_ended", null);
      int res2 = this.currentUser.getId();
      if (res2 != res1.getWinnerId() && res2 != res1.getSellerId())
        return new Response(rid, Response.ERROR, "not_participant", null);
      if (this.ratingDao.hasRated(res.getItemId(), res2))
        return new Response(rid, Response.ERROR, "already_rated", null);
      res.setRaterUserId(res2);
      if (res2 == res1.getWinnerId()) {
        res.setRatedUserId(res1.getSellerId());
      } else {
        res.setRatedUserId(res1.getWinnerId());
      }
      boolean res3 = this.ratingDao.insertRating(res);
      if (res3) {
        this.ratingDao.recalcUserRating(res.getRatedUserId());
        return new Response(rid, Response.OK, "success", null);
      }
      return new Response(rid, Response.ERROR, "fail", null);
    } catch (Exception e) {
      return new Response(rid, Response.ERROR, "fail", null);
    }
  }

  private Response handleAddLot(Request req) {
    try {
      Map<String, String> res = (Map<String, String>) req.getPayload();
      java.time.format.DateTimeFormatter f =
              java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm[:ss]");
      java.time.LocalDateTime st =
              java.time.LocalDateTime.parse(
                      res.get("starttime").length() == 16
                              ? res.get("starttime") + ":00"
                              : res.get("starttime"),
                      f);
      java.time.LocalDateTime et =
              java.time.LocalDateTime.parse(
                      res.get("endtime").length() == 16 ? res.get("endtime") + ":00" : res.get("endtime"),
                      f);

      boolean ok =
              this.itemDao.insertLot(
                      res.get("name"),
                      res.get("description"),
                      Double.parseDouble(res.get("startingprice")),
                      Double.parseDouble(res.getOrDefault("maxprice", "0")),
                      st,
                      et,
                      res.get("sellerusername"),
                      res.getOrDefault("imageurl", ""),
                      res.getOrDefault("category", "Vehicle"));
      return new Response(
              req.getRequestId(), ok ? Response.OK : Response.ERROR, ok ? "success" : "fail", null);
    } catch (Exception e) {
      return new Response(req.getRequestId(), Response.ERROR, "fail", null);
    }
  }

  private Response handleDeposit(Request req) {
    Map<String, String> res = (Map<String, String>) req.getPayload();
    int id = Integer.parseInt(res.get("userid"));
    double val = Double.parseDouble(res.get("amount"));
    com.auction.server.dao.UserDao d = new com.auction.server.dao.UserDao();
    User u = d.getById(String.valueOf(id));
    if (u != null && d.updateBalance(id, u.getBalance() + val)) {
      this.logDao.insertLog(id, "DEPOSIT", val, 0);
      u.setBalance(u.getBalance() + val);
      return new Response(req.getRequestId(), Response.OK, "success", u);
    }
    return new Response(req.getRequestId(), Response.ERROR, "fail", null);
  }

  public void send(Response r) {
    try {
      synchronized (this.out) {
        this.out.reset();
        this.out.writeObject(r);
        this.out.flush();
      }
    } catch (Exception e) {
    }
  }
}