package com.auction.client.service;

import com.auction.client.network.NetworkClient;
import com.auction.shared.*;
import java.util.List;

public class BiddingClientService {

  public Response placeBid(BidTransaction bid) {
    Request request = new Request(Request.BID, bid);
    return NetworkClient.getInstance().sendRequestAndWait(request);
  }

  public Item getItemById(int itemId) {
    Request request = new Request(Request.GET_ITEM_BY_ID, itemId);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    if (response != null && Response.OK.equals(response.getStatus())) {
      return (Item) response.getPayload();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public List<BidTransaction> getBidHistory(int itemId) {
    Request request = new Request(Request.GET_BID_HISTORY, itemId);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    if (response != null && Response.OK.equals(response.getStatus())) {
      return (List<BidTransaction>) response.getPayload();
    }
    return List.of();
  }

  @SuppressWarnings("unchecked")
  public List<Rating> getRatings(int itemId) {
    Request request = new Request(Request.GET_RATINGS, itemId);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    if (response != null && Response.OK.equals(response.getStatus())) {
      return (List<Rating>) response.getPayload();
    }
    return List.of();
  }

  public boolean submitRating(Rating rating) {
    Request request = new Request(Request.SUBMIT_RATING, rating);
    Response response = NetworkClient.getInstance().sendRequestAndWait(request);
    return response != null && Response.OK.equals(response.getStatus());
  }
}
