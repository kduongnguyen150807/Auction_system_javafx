package com.auction.client.service.auction;

import com.auction.client.store.lotsinformation.OngoingLots;
import com.auction.client.util.RequestHelper;
import com.auction.shared.Item;
import com.auction.shared.LeaderboardEntry;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.List;

public class AuctionDiscoveryService {

  public List<Item> refreshItems() {
    return RequestHelper.sendRequest(Request.GET_ONGOING_LOTS, null)
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof List<?> ongoingLots) {
          OngoingLots.AUCTION_STORE.loadOngoingItems((List<Item>) ongoingLots);
          return (List<Item>) ongoingLots;
        }
        return null;
      }).join();
  }

  public List<LeaderboardEntry> getLeaderboard() {
    return RequestHelper.sendRequest(Request.GET_LEADERBOARD, null)
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof List<?> osl) {
          return (List<LeaderboardEntry>) osl;
        }
        return null;
      }).join();
  }
}