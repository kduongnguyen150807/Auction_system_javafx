package com.auction.server.service;

import com.auction.server.controller.ClientHandler;
import com.auction.server.dao.BidDao;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.TransactionLogDao;
import com.auction.server.dao.UserDao;
import com.auction.shared.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {
  private static AuctionManager instance;
  private final List<ClientHandler> clients;
  private final BidService bidservice;
  private final ItemDao itemDao;
  private final UserDao userDao;
  private final TransactionLogDao logDao;
  private final BidDao bidDao;
  private final java.util.Map<Integer, java.util.PriorityQueue<BidTransaction>> autobids;

  private AuctionManager() {
    this.clients = new CopyOnWriteArrayList<>();
    this.bidservice = new BidService();
    this.itemDao = new ItemDao();
    this.userDao = new UserDao();
    this.logDao = new TransactionLogDao();
    this.autobids = new java.util.concurrent.ConcurrentHashMap<>();
    this.bidDao = new BidDao();
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
    Item item = itemDao.getById(b.getItemId());
    User bidder = userDao.getById(String.valueOf(b.getUserId()));

    Response error = validateBidRequest(item, bidder, b);
    if (error != null) {
      return error;
    }

    normalizeBidValueIfNeeded(item, b);

    if (bidder.getBalance() < b.getBidValue()) {
      return new Response("", Response.ERROR, "Fail", null);
    }

    if (isBuyNow(item, b)) {
      return handleBuyNow(item, bidder, b);
    }

    return handleNormalBid(item, bidder, b);
  }

  private Response validateBidRequest(Item item, User bidder, BidTransaction b) {
    if (item == null) {
      return new Response("", Response.ERROR, "item_not_found", null);
    }

    if (bidder == null) {
      return new Response("", Response.ERROR, "user_not_found", null);
    }

    String phone = bidder.getPhoneNumber();
    if (phone == null || phone.trim().isEmpty()) {
      return new Response(
              "", Response.ERROR, "Unverified account. Add a phone number to bid.", null);
    }

    if (item.getSellerId() == b.getUserId()) {
      return new Response("", Response.ERROR, "Fail", null);
    }

    return null;
  }

  private void normalizeBidValueIfNeeded(Item item, BidTransaction b) {
    if (b.isAutoBid()) {
      b.setBidValue(item.getCurrentPrice() + b.getAutoBidIncrement());
    }

    if (b.getBidValue() <= item.getCurrentPrice()) {
      b.setBidValue(item.getCurrentPrice() + b.getAutoBidIncrement());
    }
  }

  private boolean isBuyNow(Item item, BidTransaction b) {
    return item.getMaxPrice() > 0 && b.getBidValue() >= item.getMaxPrice();
  }

  private Response handleBuyNow(Item item, User bidder, BidTransaction b) {
    double price = item.getMaxPrice();

    try (java.sql.Connection conn =
                 com.auction.server.dao.DatabaseConnection.getInstance().getConnection()) {
      conn.setAutoCommit(false);

      try {
        User seller = userDao.getById(String.valueOf(item.getSellerId()));

        boolean okBuyerBalance =
                userDao.updateBalance(conn, bidder.getId(), bidder.getBalance() - price);
        if (!okBuyerBalance) {
          conn.rollback();
          return new Response("", Response.ERROR, "fail", null);
        }

        boolean okBuyerMetrics = userDao.addBidderMetrics(conn, bidder.getId(), price);
        if (!okBuyerMetrics) {
          conn.rollback();
          return new Response("", Response.ERROR, "fail", null);
        }

        boolean okBuyerLog = logDao.insertLog(conn, bidder.getId(), "ITEM_BOUGHT", -price, b.getItemId());
        if (!okBuyerLog) {
          conn.rollback();
          return new Response("", Response.ERROR, "fail", null);
        }

        if (seller != null) {
          boolean okSellerBalance =
                  userDao.updateBalance(conn, seller.getId(), seller.getBalance() + price);
          if (!okSellerBalance) {
            conn.rollback();
            return new Response("", Response.ERROR, "fail", null);
          }

          boolean okSellerMetrics = userDao.addSellerMetrics(conn, seller.getId(), price);
          if (!okSellerMetrics) {
            conn.rollback();
            return new Response("", Response.ERROR, "fail", null);
          }

          boolean okSellerLog =
                  logDao.insertLog(conn, seller.getId(), "ITEM_SOLD", price, b.getItemId());
          if (!okSellerLog) {
            conn.rollback();
            return new Response("", Response.ERROR, "fail", null);
          }
        }

        boolean okPrice = itemDao.updatePrice(conn, item.getId(), price, item.getVersion());
        if (!okPrice) {
          conn.rollback();
          return new Response("", Response.ERROR, "conflict", null);
        }

        itemDao.closeAuction(conn, item.getId(), b.getUserId(), "CLOSED");

        conn.commit();

      } catch (Exception e) {
        conn.rollback();
        e.printStackTrace();
        return new Response("", Response.ERROR, "fail", null);
      } finally {
        conn.setAutoCommit(true);
      }

    } catch (Exception e) {
      e.printStackTrace();
      return new Response("", Response.ERROR, "fail", null);
    }

    sendUpdatedBalance(bidder.getId(), "Success");

    User seller = userDao.getById(String.valueOf(item.getSellerId()));
    if (seller != null) {
      sendUpdatedBalance(seller.getId(), "Success");
    }

    Response result = new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", b.getItemId());
    broadcast(result);

    int previousHighestBidderId = getPreviousHighestBidder(b.getItemId());
    if (previousHighestBidderId > 0) {
      sendToUser(
              previousHighestBidderId,
              new Response("", "OUTBID_NOTIFY", "outbid", b.getItemId()));
    }

    broadcastLatestItemState(b.getItemId());
    return result;
  }

  private Response handleNormalBid(Item item, User bidder, BidTransaction b) {
    int previousHighestBidderId = getPreviousHighestBidder(b.getItemId());
    double previousPrice = item.getCurrentPrice();

    Response result = this.bidservice.placeBid(b);

    if (Response.OK.equals(result.getStatus())) {
      holdBidderMoney(bidder, b);
      refundPreviousHighestBidder(previousHighestBidderId, previousPrice, b.getItemId());
      afterBidSuccess(item.getId(), previousHighestBidderId, b, result);
    }

    return result;
  }

  private void holdBidderMoney(User bidder, BidTransaction b) {
    userDao.updateBalance(bidder.getId(), bidder.getBalance() - b.getBidValue());
    logDao.insertLog(bidder.getId(), "BID_HOLD", -b.getBidValue(), b.getItemId());
    sendUpdatedBalance(bidder.getId(), "Success");
  }

  private void refundPreviousHighestBidder(
          int previousHighestBidderId, double previousPrice, int itemId) {
    if (previousHighestBidderId <= 0 || previousPrice <= 0) return;

    User oldBidder = userDao.getById(String.valueOf(previousHighestBidderId));
    if (oldBidder == null) return;

    userDao.updateBalance(previousHighestBidderId, oldBidder.getBalance() + previousPrice);
    logDao.insertLog(previousHighestBidderId, "BID_REFUND", previousPrice, itemId);
    sendToUser(
            previousHighestBidderId,
            new Response(
                    "", "BALANCE_UPDATE", "Outbid", userDao.getById(String.valueOf(previousHighestBidderId))));
  }

  private void afterBidSuccess(
          int itemId, int previousHighestBidderId, BidTransaction b, Response result) {
    Item latestItem = itemDao.getById(itemId);
    if (latestItem != null) {
      broadcast(new Response("", "NEW_BID_UPDATE", "priceupdate", latestItem));
    }

    broadcast(result);

    if (previousHighestBidderId > 0) {
      sendToUser(
              previousHighestBidderId,
              new Response("", "OUTBID_NOTIFY", "outbid", itemId));
    }

    if (b.isAutoBid()) {
      registerAutoBidQueue(b);
    }

    tryProcessAutoCounters(itemId);
  }

  private void sendUpdatedBalance(int userId, String message) {
    sendToUser(
            userId,
            new Response(
                    "", "BALANCE_UPDATE", message, userDao.getById(String.valueOf(userId))));
  }

  private void broadcastLatestItemState(int itemId) {
    Item latestItem = itemDao.getById(itemId);
    if (latestItem != null) {
      broadcast(new Response("", "NEW_BID_UPDATE", "priceupdate", latestItem));
    }
  }

  /** Ghi nhận trần auto-bid sau khi đặt giá thành công */
  private void registerAutoBidQueue(BidTransaction b) {
    java.util.PriorityQueue<BidTransaction> q =
            this.autobids.computeIfAbsent(
                    b.getItemId(),
                    k ->
                            new java.util.PriorityQueue<>(
                                    10, (a1, a2) -> Double.compare(a2.getMaxAutoBid(), a1.getMaxAutoBid())));
    q.add(b);
  }

  /**
   * Sau mỗi lần giá thay đổi thành công: nếu có người đăng ký auto-bid (không phải người đang dẫn)
   * và trần cho phép, đặt giúp một bước.
   */
  private void tryProcessAutoCounters(int itemId) {
    java.util.PriorityQueue<BidTransaction> queue = this.autobids.get(itemId);
    if (queue == null || queue.isEmpty()) return;

    Item currentItem = itemDao.getById(itemId);
    if (currentItem == null) return;

    double currentPrice = currentItem.getCurrentPrice();
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
    return bidDao.getHighestBidderId(id);
  }

  public void sendToUser(int id, Response r) {
    for (ClientHandler client : this.clients) {
      if (client.getCurrentUser() != null && client.getCurrentUser().getId() == id) {
        client.send(r);
      }
    }
  }

  public void broadcast(Response r) {
    for (ClientHandler client : this.clients) {
      client.send(r);
    }
  }
}