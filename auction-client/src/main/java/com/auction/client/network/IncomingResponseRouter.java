package com.auction.client.network;

import com.auction.shared.ChatMessage;
import com.auction.shared.Friendship;
import com.auction.shared.Item;
import com.auction.shared.ItemFactory;
import com.auction.shared.LeaderboardEntry;
import com.auction.shared.LiveParticipantEvent;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Điều hướng các Response từ Server tới các Listener hoặc các Future đang đợi.
 */
final class IncomingResponseRouter {

  private static final Logger logger = LoggerFactory.getLogger(IncomingResponseRouter.class);

  private final ConcurrentHashMap<String, CompletableFuture<Response>> pendingMap;
  private final List<NetworkEventListener> listeners;

  IncomingResponseRouter(
          ConcurrentHashMap<String, CompletableFuture<Response>> pendingMap,
          List<NetworkEventListener> listeners) {
    this.pendingMap = pendingMap;
    this.listeners = listeners;
  }

  void dispatch(Response response) {
    if (response == null) {
      return;
    }

    String status = response.getStatus();
    Object payload = response.getPayload();

    // 1. Xử lý các sự kiện Real-time (Server Push)
    switch (status) {
      case "BALANCE_UPDATE" -> {
        if (payload instanceof User u) {
          forListeners(l -> l.onBalanceUpdate(u));
        }
        return;
      }
      case "OUTBID_NOTIFY" -> {
        handleOutbid(payload);
        return;
      }
      case "CHAT_GLOBAL" -> {
        if (payload instanceof ChatMessage m) {
          forListeners(l -> l.onGlobalChat(m));
        }
        return;
      }
      case "CHAT_PRIVATE" -> {
        if (payload instanceof ChatMessage m) {
          forListeners(l -> l.onPrivateChat(m));
        }
        return;
      }
      case "FRIEND_REQUEST" -> {
        if (payload instanceof Friendship f) {
          forListeners(l -> l.onFriendRequest(f));
        }
        return;
      }
      case "ITEM_CLOSED" -> {
        if (payload instanceof Item item) {
          forListeners(l -> l.onItemClosed(item));
        }
        return;
      }
      case "NEW_BID_UPDATE" -> {
        if (payload instanceof Item item) {
          forListeners(l -> l.onNewBidUpdate(item));
        }
        return;
      }
      case Response.ACCOUNT_BANNED -> {
        forListeners(l -> l.onAccountBanned(response.getMessage()));
        return;
      }
      case "LEADERBOARD_UPDATE" -> {
        handleLeaderboard(payload);
        return;
      }
      case "LIVE_PARTICIPANT" -> {
        if (payload instanceof LiveParticipantEvent event) {
          forListeners(l -> l.onLiveParticipantUpdate(event));
        }
        return;
      }
      default -> {
        // Tiếp tục xử lý nếu không phải Push event
      }
    }

    // 2. Xử lý phản hồi cho Request (Dựa trên requestId)
    String requestId = response.getRequestId();
    if (requestId != null) {
      CompletableFuture<Response> future = pendingMap.remove(requestId);
      if (future != null) {
        future.complete(response);
      } else {
        logger.warn("Nhận được phản hồi cho requestId không tồn tại: {}", requestId);
      }
    }
  }

  private void handleOutbid(Object payload) {
    if (payload instanceof Item item) {
      forListeners(l -> l.onOutbidNotify(item));
    } else if (payload instanceof Integer id) {
      Item stub = ItemFactory.createItem(null);
      stub.setId(id);
      stub.setName("?");
      forListeners(l -> l.onOutbidNotify(stub));
    }
  }

  @SuppressWarnings("unchecked")
  private void handleLeaderboard(Object payload) {
    if (payload instanceof List<?> rawList) {
      List<LeaderboardEntry> entries = new ArrayList<>();
      for (Object obj : rawList) {
        if (obj instanceof LeaderboardEntry e) {
          entries.add(e);
        }
      }
      forListeners(l -> l.onLeaderboardUpdate(entries));
    }
  }

  private void forListeners(Consumer<NetworkEventListener> action) {
    // Luôn cập nhật UI trên JavaFX Application Thread
    Platform.runLater(() -> listeners.forEach(action));
  }
}