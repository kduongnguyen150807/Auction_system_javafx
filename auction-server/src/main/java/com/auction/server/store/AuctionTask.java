package com.auction.server.store;

import com.auction.server.service.AuctionService;
import com.auction.shared.BidResult;
import com.auction.shared.dto.BidForm;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class AuctionTask<T> {
  private final AuctionTaskType taskType;
  private final Callable<T> action;
  private final CompletableFuture<T> future;

  public AuctionTask(AuctionTaskType auctionTaskType, Callable<T> action) {
    this.taskType = auctionTaskType;
    this.action = action;
    this.future = new CompletableFuture<>();
  }

  public void execute() {
    try {
      T result = action.call();
      future.complete(result);
    } catch (Exception e) {
      future.completeExceptionally(e);
    }
  }

  public CompletableFuture<T> getFuture() {
    return future;
  }
}
