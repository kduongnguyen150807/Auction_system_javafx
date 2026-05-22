package com.auction.client.service;

import com.auction.client.network.NetworkClient;
import com.auction.shared.*;
import java.util.List;

public class LiveAuctionClientService {

  @SuppressWarnings("unchecked")
  public List<Item> fetchLiveAuctions() {
    Request request = new Request(Request.GET_LIVE_AUCTIONS, null);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    if (response != null && Response.OK.equals(response.getStatus()) && response.getPayload() instanceof List<?> list) {
      return (List<Item>) list;
    }
    return List.of();
  }

  public LiveSessionInfo joinSession(int itemId) {
    Request request = new Request(Request.JOIN_LIVE_SESSION, itemId);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    if (response != null && Response.OK.equals(response.getStatus()) && response.getPayload() instanceof LiveSessionInfo info) {
      return info;
    }
    return null;
  }

  public void leaveSession() {
    Request request = new Request(Request.LEAVE_LIVE_SESSION, null);
    NetworkClient.getInstance().sendRequestAndWait(request);
  }

  public LiveBidTiers fetchBidTiers(int itemId) {
    Request request = new Request(Request.GET_LIVE_BID_TIERS, itemId);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    if (response != null && Response.OK.equals(response.getStatus()) && response.getPayload() instanceof LiveBidTiers tiers) {
      return tiers;
    }
    return null;
  }

  public Response placeBid(BidTransaction bid) {
    Request request = new Request(Request.BID, bid);
    return NetworkClient.getInstance().sendRequestAndWait(request);
  }
}
