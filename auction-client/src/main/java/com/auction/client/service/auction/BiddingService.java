package com.auction.client.service.auction;

import com.auction.client.util.RequestHelper;
import com.auction.shared.BidTransaction;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.concurrent.CompletableFuture;

public class BiddingService {

  public CompletableFuture<Response> placeBid(BidTransaction bidTransaction) {
    if (bidTransaction == null) {
      return CompletableFuture.failedFuture(new IllegalArgumentException("INVALID BID"));
    }

    return RequestHelper.sendRequest(Request.BID, bidTransaction);
  }
}