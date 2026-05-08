package com.auction.client.network;

import com.auction.shared.ChatMessage;
import com.auction.shared.Friendship;
import com.auction.shared.Item;
import com.auction.shared.ItemFactory;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps inbound {@link Response} payloads to {@link NetworkEventListener} callbacks and completes
 * pending request futures.
 */
final class IncomingResponseRouter {
  private static final Logger LOGGER = LoggerFactory.getLogger(IncomingResponseRouter.class);

  private final ConcurrentHashMap<String, CompletableFuture<Response>> pendingMap;
  private final List<NetworkEventListener> listeners;

  IncomingResponseRouter(
      ConcurrentHashMap<String, CompletableFuture<Response>> pendingMap,
      List<NetworkEventListener> listeners) {
    this.pendingMap = pendingMap;
    this.listeners = listeners;
  }

  void dispatch(Response response) {
    switch (response.getStatus()) {
      case "BALANCE_UPDATE" -> {
        if (response.getPayload() instanceof User u) forListeners(l -> l.onBalanceUpdate(u));
        return;
      }
      case "OUTBID_NOTIFY" -> {
        if (response.getPayload() instanceof Item item) {
          forListeners(l -> l.onOutbidNotify(item));
        } else if (response.getPayload() instanceof Integer id) {
          Item stub = ItemFactory.createItem(null);
          stub.setId(id);
          stub.setName("?");
          forListeners(l -> l.onOutbidNotify(stub));
        }
        return;
      }
      case "CHAT_GLOBAL" -> {
        if (response.getPayload() instanceof ChatMessage m) forListeners(l -> l.onGlobalChat(m));
        return;
      }
      case "CHAT_PRIVATE" -> {
        if (response.getPayload() instanceof ChatMessage m) forListeners(l -> l.onPrivateChat(m));
        return;
      }
      case "FRIEND_REQUEST" -> {
        if (response.getPayload() instanceof Friendship f) forListeners(l -> l.onFriendRequest(f));
        return;
      }
      case "FRIEND_REQUEST_SENT" -> {
        if (response.getPayload() instanceof Friendship f) forListeners(l -> l.onFriendRequestSent(f));
        return;
      }
      case "FRIEND_ACCEPTED" -> {
        if (response.getPayload() instanceof Friendship f) forListeners(l -> l.onFriendAccepted(f));
        return;
      }
      case "SELLER_BID_NOTIFY" -> {
        if (response.getPayload() instanceof Item item)
          forListeners(l -> l.onSellerBidNotify(item, item.getCurrentPrice()));
        return;
      }
      case "ITEM_CLOSED" -> {
        if (response.getPayload() instanceof Item item) forListeners(l -> l.onItemClosed(item));
        return;
      }
      case "NEW_BID_UPDATE" -> {
        if (response.getPayload() instanceof Item item) forListeners(l -> l.onNewBidUpdate(item));
        return;
      }
      case Response.ACCOUNT_BANNED -> {
        forListeners(l -> l.onAccountBanned(response.getMessage()));
        return;
      }
      case Response.ACCOUNT_UNBANNED -> {
        forListeners(NetworkEventListener::onAccountUnbanned);
        return;
      }
      default -> {}
    }
    if ("priceupdate".equals(response.getMessage()) && response.getPayload() instanceof Item item) {
      forListeners(l -> l.onNewBidUpdate(item));
      return;
    }
    String requestId = response.getRequestId();
    if (requestId == null) return;
    CompletableFuture<Response> future = pendingMap.get(requestId);
    if (future != null) future.complete(response);
    else LOGGER.warn("Received response for unknown request ID: {}", requestId);
  }

  private void forListeners(Consumer<NetworkEventListener> action) {
    Platform.runLater(() -> listeners.forEach(action));
  }
}
