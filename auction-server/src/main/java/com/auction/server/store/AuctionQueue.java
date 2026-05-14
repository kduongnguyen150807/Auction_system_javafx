package com.auction.server.store;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AuctionQueue {
  private final BlockingQueue<AuctionTask> queue = new LinkedBlockingQueue<>(10);
  private final int itemId;

  public AuctionQueue(int itemId) {
    this.itemId = itemId;
  }

  public void submit(AuctionTask bidTask) {
    queue.offer(bidTask);
  }

  public void process() {
    Thread.startVirtualThread(() -> {
      while (true) {
        try {
          AuctionTask task = queue.take();
          task.execute();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }
}
