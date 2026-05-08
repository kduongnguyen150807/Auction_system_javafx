package com.auction.server.service.auction;

import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.user.UserDao;
import com.auction.shared.Item;
import com.auction.shared.Response;
import com.auction.shared.User;

/** Push realtime auction events and balance snapshots to connected clients. */
final class AuctionRealtimeNotifier {

  private final ItemDao itemDao;
  private final UserDao userDao;
  private final ClientConnectionHub hub;

  AuctionRealtimeNotifier(ItemDao itemDao, UserDao userDao, ClientConnectionHub hub) {
    this.itemDao = itemDao;
    this.userDao = userDao;
    this.hub = hub;
  }

  void broadcastPriceUpdate(int itemId) {
    Item item = itemDao.getById(itemId);
    if (item == null) return;
    hub.broadcast(new Response("", "NEW_BID_UPDATE", "priceupdate", item));
    hub.sendToUser(item.getSellerId(), new Response("", "SELLER_BID_NOTIFY", "new_bid_on_your_item", item));
  }

  void broadcastItemClosed(int itemId) {
    Item item = itemDao.getById(itemId);
    if (item != null) hub.broadcast(new Response("", "ITEM_CLOSED", "closed", item));
  }

  void sendBalanceUpdateToUser(int userId) {
    User u = userDao.getById(String.valueOf(userId));
    if (u != null) hub.sendToUser(userId, new Response("", "BALANCE_UPDATE", "Success", u));
  }

  void notifyOutbidUser(int userId, int itemId) {
    Item snapshot = itemDao.getById(itemId);
    if (snapshot != null)
      hub.sendToUser(userId, new Response("", "OUTBID_NOTIFY", "outbid", snapshot));
    else hub.sendToUser(userId, new Response("", "OUTBID_NOTIFY", "outbid", itemId));
  }
}
