package com.auction.server.live;

import com.auction.server.service.auction.AuctionManager;
import com.auction.shared.LiveParticipantEvent;
import com.auction.shared.LiveParticipantSummary;
import com.auction.shared.LiveSessionInfo;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/** In-memory registry of users participating in live auction rooms. */
public final class LiveSessionManager {
  private static final LiveSessionManager INSTANCE = new LiveSessionManager();

  private final ConcurrentHashMap<Integer, CopyOnWriteArraySet<Integer>> itemParticipants =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Integer, Integer> userActiveItem = new ConcurrentHashMap<>();

  private LiveSessionManager() {}

  public static LiveSessionManager getInstance() {
    return INSTANCE;
  }

  /** Clears in-memory session state between tests. */
  public static void resetForTest() {
    INSTANCE.itemParticipants.clear();
    INSTANCE.userActiveItem.clear();
  }

  public synchronized LiveSessionInfo join(User user, int itemId, int udpPort) {
    leaveInternal(user.getId());
    itemParticipants
        .computeIfAbsent(itemId, k -> new CopyOnWriteArraySet<>())
        .add(user.getId());
    userActiveItem.put(user.getId(), itemId);
    LiveSessionInfo info = buildSessionInfo(itemId, udpPort);
    broadcastParticipantEvent(
        new LiveParticipantEvent(
            itemId,
            user.getId(),
            user.getUsername(),
            user.getAvatarUrl(),
            LiveParticipantEvent.Action.JOINED));
    return info;
  }

  public synchronized void leave(int userId) {
    Integer itemId = userActiveItem.remove(userId);
    if (itemId == null) {
      return;
    }
    removeFromItem(userId, itemId);
    User user = AuctionManager.getInstance().getUserDao().getById(String.valueOf(userId));
    if (user != null) {
      broadcastParticipantEvent(
          new LiveParticipantEvent(
              itemId,
              userId,
              user.getUsername(),
              user.getAvatarUrl(),
              LiveParticipantEvent.Action.LEFT));
    }
  }

  public boolean isParticipant(int itemId, int userId) {
    Set<Integer> set = itemParticipants.get(itemId);
    return set != null && set.contains(userId);
  }

  public LiveSessionInfo buildSessionInfo(int itemId, int udpPort) {
    Set<Integer> ids = itemParticipants.getOrDefault(itemId, new CopyOnWriteArraySet<>());
    List<LiveParticipantSummary> participants = new ArrayList<>();
    for (Integer uid : ids) {
      User u = AuctionManager.getInstance().getUserDao().getById(String.valueOf(uid));
      if (u != null) {
        participants.add(new LiveParticipantSummary(u.getId(), u.getUsername(), u.getAvatarUrl()));
      }
    }
    return new LiveSessionInfo(itemId, udpPort, participants.size(), participants);
  }

  public Set<Integer> getParticipants(int itemId) {
    return itemParticipants.getOrDefault(itemId, new CopyOnWriteArraySet<>());
  }

  private void leaveInternal(int userId) {
    Integer oldItem = userActiveItem.remove(userId);
    if (oldItem != null) {
      removeFromItem(userId, oldItem);
    }
  }

  private void removeFromItem(int userId, int itemId) {
    Set<Integer> set = itemParticipants.get(itemId);
    if (set != null) {
      set.remove(userId);
      if (set.isEmpty()) {
        itemParticipants.remove(itemId, set);
      }
    }
    VideoRelayServer.getInstance().unregisterUser(itemId, userId);
  }

  private void broadcastParticipantEvent(LiveParticipantEvent event) {
    Set<Integer> ids = getParticipants(event.getItemId());
    Response push = new Response("", "LIVE_PARTICIPANT", "update", event);
    for (Integer uid : ids) {
      AuctionManager.getInstance().sendToUser(uid, push);
    }
  }

  /** For tests / admin — snapshot of active rooms. */
  Map<Integer, Set<Integer>> snapshot() {
    Map<Integer, Set<Integer>> copy = new ConcurrentHashMap<>();
    itemParticipants.forEach((k, v) -> copy.put(k, Collections.unmodifiableSet(v)));
    return copy;
  }
}
