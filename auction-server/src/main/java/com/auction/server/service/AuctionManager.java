package com.auction.server.service;

import com.auction.server.controller.ClientHandler;
import com.auction.server.dao.BidDao;
import com.auction.server.dao.DatabaseConnection;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.TransactionLogDao;
import com.auction.server.dao.UserDao;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuctionManager {
  private static final Logger LOGGER = Logger.getLogger(AuctionManager.class.getName());

  private static final String BALANCE_UPDATE = "BALANCE_UPDATE";
  private static final String NEW_BID_UPDATE = "NEW_BID_UPDATE";
  private static final String OUTBID_NOTIFY = "OUTBID_NOTIFY";
  private static final String ITEM_BOUGHT = "ITEM_BOUGHT";
  private static final String ITEM_SOLD = "ITEM_SOLD";
  private static final String BID_HOLD = "BID_HOLD";
  private static final String BID_REFUND = "BID_REFUND";

  private static AuctionManager instance;

  private final List<ClientHandler> clients;
  private final BidService bidService;
  private final ItemDao itemDao;
  private final UserDao userDao;
  private final TransactionLogDao logDao;
  private final BidDao bidDao;
  private final Map<Integer, PriorityQueue<BidTransaction>> autoBids;

  private AuctionManager() {
    this.clients = new CopyOnWriteArrayList<>();
    this.bidService = new BidService();
    this.itemDao = new ItemDao();
    this.userDao = new UserDao();
    this.logDao = new TransactionLogDao();
    this.bidDao = new BidDao();
    this.autoBids = new ConcurrentHashMap<>();
  }

  public static synchronized AuctionManager getInstance() {
    if (instance == null) {
      instance = new AuctionManager();
    }
    return instance;
  }

  public void addClient(ClientHandler client) {
    clients.add(client);
  }

  public void removeClient(ClientHandler client) {
    clients.remove(client);
  }

  public synchronized Response processBid(BidTransaction bid) {
    Item item = itemDao.getById(bid.getItemId());
    User bidder = userDao.getById(String.valueOf(bid.getUserId()));

    Response validationError = validateBidRequest(item, bidder, bid);
    if (validationError != null) {
      return validationError;
    }

    normalizeBidValue(item, bid);

    if (bidder.getBalance() < bid.getBidValue()) {
      return error("Fail");
    }

    if (isBuyNow(item, bid)) {
      return handleBuyNow(item, bidder, bid);
    }

    return handleNormalBid(item, bidder, bid);
  }

  private Response validateBidRequest(Item item, User bidder, BidTransaction bid) {
    if (item == null) {
      return error("item_not_found");
    }

    if (bidder == null) {
      return error("user_not_found");
    }

    String phone = bidder.getPhoneNumber();
    if (phone == null || phone.trim().isEmpty()) {
      return error("Unverified account. Add a phone number to bid.");
    }

    if (item.getSellerId() == bid.getUserId()) {
      return error("Fail");
    }

    return null;
  }

  private void normalizeBidValue(Item item, BidTransaction bid) {
    if (bid.isAutoBid() || bid.getBidValue() <= item.getCurrentPrice()) {
      bid.setBidValue(item.getCurrentPrice() + bid.getAutoBidIncrement());
    }
  }

  private boolean isBuyNow(Item item, BidTransaction bid) {
    return item.getMaxPrice() > 0 && bid.getBidValue() >= item.getMaxPrice();
  }

  private Response handleBuyNow(Item item, User buyer, BidTransaction bid) {
    double price = item.getMaxPrice();

    try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
      connection.setAutoCommit(false);

      try {
        Response transactionError = completeBuyNowTransaction(connection, item, buyer, bid, price);
        if (transactionError != null) {
          connection.rollback();
          return transactionError;
        }

        connection.commit();
      } catch (Exception e) {
        connection.rollback();
        LOGGER.log(Level.WARNING, "Failed to complete buy-now transaction", e);
        return error("fail");
      } finally {
        connection.setAutoCommit(true);
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to open buy-now transaction", e);
      return error("fail");
    }

    notifyBuyNowSuccess(item, buyer, bid);
    return new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", bid.getItemId());
  }

  private Response completeBuyNowTransaction(
          Connection connection, Item item, User buyer, BidTransaction bid, double price) {
    User seller = userDao.getById(String.valueOf(item.getSellerId()));

    boolean buyerUpdated =
            userDao.updateBalance(connection, buyer.getId(), buyer.getBalance() - price);
    boolean buyerMetricsUpdated =
            userDao.addBidderMetrics(connection, buyer.getId(), price);
    boolean buyerLogInserted =
            logDao.insertLog(connection, buyer.getId(), ITEM_BOUGHT, -price, bid.getItemId());

    if (!buyerUpdated || !buyerMetricsUpdated || !buyerLogInserted) {
      return error("fail");
    }

    if (seller != null) {
      boolean sellerUpdated =
              userDao.updateBalance(connection, seller.getId(), seller.getBalance() + price);
      boolean sellerMetricsUpdated =
              userDao.addSellerMetrics(connection, seller.getId(), price);
      boolean sellerLogInserted =
              logDao.insertLog(connection, seller.getId(), ITEM_SOLD, price, bid.getItemId());

      if (!sellerUpdated || !sellerMetricsUpdated || !sellerLogInserted) {
        return error("fail");
      }
    }

    boolean priceUpdated = itemDao.updatePrice(connection, item.getId(), price, item.getVersion());
    if (!priceUpdated) {
      return error("conflict");
    }

    itemDao.closeAuction(connection, item.getId(), bid.getUserId(), "CLOSED");
    return null;
  }

  private void notifyBuyNowSuccess(Item item, User buyer, BidTransaction bid) {
    sendUpdatedBalance(buyer.getId(), "Success");

    User seller = userDao.getById(String.valueOf(item.getSellerId()));
    if (seller != null) {
      sendUpdatedBalance(seller.getId(), "Success");
    }

    Response result = new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", bid.getItemId());
    broadcast(result);

    int previousHighestBidderId = getPreviousHighestBidder(bid.getItemId());
    notifyOutbidUser(previousHighestBidderId, bid.getItemId());
    broadcastLatestItemState(bid.getItemId());
  }

  private Response handleNormalBid(Item item, User bidder, BidTransaction bid) {
    int previousHighestBidderId = getPreviousHighestBidder(bid.getItemId());
    double previousPrice = item.getCurrentPrice();

    Response result = bidService.placeBid(bid);
    if (!Response.OK.equals(result.getStatus())) {
      return result;
    }

    holdBidderMoney(bidder, bid);
    refundPreviousHighestBidder(previousHighestBidderId, previousPrice, bid.getItemId());
    notifyBidSuccess(item.getId(), previousHighestBidderId, bid, result);

    return result;
  }

  private void holdBidderMoney(User bidder, BidTransaction bid) {
    userDao.updateBalance(bidder.getId(), bidder.getBalance() - bid.getBidValue());
    logDao.insertLog(bidder.getId(), BID_HOLD, -bid.getBidValue(), bid.getItemId());
    sendUpdatedBalance(bidder.getId(), "Success");
  }

  private void refundPreviousHighestBidder(
          int previousHighestBidderId, double previousPrice, int itemId) {
    if (previousHighestBidderId <= 0 || previousPrice <= 0) {
      return;
    }

    User previousBidder = userDao.getById(String.valueOf(previousHighestBidderId));
    if (previousBidder == null) {
      return;
    }

    userDao.updateBalance(previousHighestBidderId, previousBidder.getBalance() + previousPrice);
    logDao.insertLog(previousHighestBidderId, BID_REFUND, previousPrice, itemId);
    sendUpdatedBalance(previousHighestBidderId, "Outbid");
  }

  private void notifyBidSuccess(
          int itemId, int previousHighestBidderId, BidTransaction bid, Response result) {
    broadcastLatestItemState(itemId);
    broadcast(result);
    notifyOutbidUser(previousHighestBidderId, itemId);

    if (bid.isAutoBid()) {
      registerAutoBidQueue(bid);
    }

    tryProcessAutoCounters(itemId);
  }

  private void sendUpdatedBalance(int userId, String message) {
    sendToUser(
            userId,
            new Response("", BALANCE_UPDATE, message, userDao.getById(String.valueOf(userId))));
  }

  private void notifyOutbidUser(int userId, int itemId) {
    if (userId > 0) {
      sendToUser(userId, new Response("", OUTBID_NOTIFY, "outbid", itemId));
    }
  }

  private void broadcastLatestItemState(int itemId) {
    Item latestItem = itemDao.getById(itemId);
    if (latestItem != null) {
      broadcast(new Response("", NEW_BID_UPDATE, "priceupdate", latestItem));
    }
  }

  private void registerAutoBidQueue(BidTransaction bid) {
    PriorityQueue<BidTransaction> queue =
            autoBids.computeIfAbsent(
                    bid.getItemId(),
                    ignored ->
                            new PriorityQueue<>(
                                    10,
                                    (left, right) ->
                                            Double.compare(right.getMaxAutoBid(), left.getMaxAutoBid())));

    queue.add(bid);
  }

  private void tryProcessAutoCounters(int itemId) {
    PriorityQueue<BidTransaction> queue = autoBids.get(itemId);
    if (queue == null || queue.isEmpty()) {
      return;
    }

    Item currentItem = itemDao.getById(itemId);
    if (currentItem == null) {
      return;
    }

    double currentPrice = currentItem.getCurrentPrice();
    int currentLeader = getPreviousHighestBidder(itemId);
    List<BidTransaction> registrations = drainQueue(queue);

    for (BidTransaction registration : registrations) {
      if (shouldAutoCounter(registration, currentLeader, currentPrice)) {
        BidTransaction counterBid = createCounterBid(itemId, registration, currentPrice);
        processBid(counterBid);
        break;
      }
    }

    queue.addAll(registrations);
  }

  private List<BidTransaction> drainQueue(PriorityQueue<BidTransaction> queue) {
    List<BidTransaction> registrations = new ArrayList<>();
    while (!queue.isEmpty()) {
      registrations.add(queue.poll());
    }
    return registrations;
  }

  private boolean shouldAutoCounter(
          BidTransaction registration, int currentLeader, double currentPrice) {
    return registration.getUserId() != currentLeader
            && registration.getMaxAutoBid() > currentPrice;
  }

  private BidTransaction createCounterBid(
          int itemId, BidTransaction registration, double currentPrice) {
    double nextBidValue = currentPrice + registration.getAutoBidIncrement();

    BidTransaction counterBid = new BidTransaction(itemId, registration.getUserId(), nextBidValue);
    counterBid.setAutoBid(false);
    counterBid.setAutoBidIncrement(registration.getAutoBidIncrement());

    return counterBid;
  }

  private int getPreviousHighestBidder(int itemId) {
    return bidDao.getHighestBidderId(itemId);
  }

  public void sendToUser(int userId, Response response) {
    for (ClientHandler client : clients) {
      if (client.getCurrentUser() != null && client.getCurrentUser().getId() == userId) {
        client.send(response);
      }
    }
  }

  public void broadcast(Response response) {
    for (ClientHandler client : clients) {
      client.send(response);
    }
  }

  private Response error(String message) {
    return new Response("", Response.ERROR, message, null);
  }
}