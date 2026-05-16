package com.auction.client.service;

import com.auction.client.util.RequestHelper;
import com.auction.shared.BidTransaction;
import com.auction.shared.Request;
import com.auction.shared.Response;

public class BiddingService {
  public static String placeBid(BidTransaction bidTransaction) {
    return RequestHelper.sendRequest(Request.BID, bidTransaction)
      .thenApply(response ->  {
        if (response.getStatus().equals(Response.OK)) {
          return response.getMessage();
        }  else {
          return response.getMessage();
        }
      }).join();
  }
}
