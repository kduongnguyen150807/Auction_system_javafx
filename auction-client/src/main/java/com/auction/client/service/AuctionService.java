package com.auction.client.service;

import com.auction.client.store.AuctionStore;
import com.auction.client.util.RequestHelper;
import com.auction.shared.Item;
import com.auction.shared.LeaderboardEntry;
import com.auction.shared.Request;
import com.auction.shared.Response;
import javafx.collections.ObservableList;

import java.util.List;

public class AuctionService {
  public static List<LeaderboardEntry> getLeaderboard() {
    return RequestHelper.sendRequest(Request.GET_LEADERBOARD, null)
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof List<?> osl) {
          return (List<LeaderboardEntry>) osl;
        } else {
          return null;
        }
      }).join();
  }

  public static List<Item> refreshItems() {
    return RequestHelper.sendRequest(Request.GET_ONGOING_LOTS, null)
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof List<?> ongoingLots) {
          AuctionStore.AUCTION_STORE.refreshItems((List<Item>) ongoingLots);
          return (List<Item>) ongoingLots;
        } else {
          return null;
        }
      }).join();
  }
}
