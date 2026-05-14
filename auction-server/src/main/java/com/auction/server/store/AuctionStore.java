package com.auction.server.store;

import com.auction.shared.dto.BidForm;
import com.auction.shared.item.Item;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class AuctionStore {
  private static final ConcurrentHashMap<Integer, AuctionQueue> queues = new ConcurrentHashMap<>();

  public static void submit(int itemId, AuctionTask task) {
    AuctionQueue queue = getQueue(itemId);
    queue.submit(task);
  }

  public static AuctionQueue getQueue(int itemId) {
    return queues.computeIfAbsent(itemId, id -> {
      AuctionQueue q = new AuctionQueue(id);
      q.process();
      return q;
    });
  }
}
