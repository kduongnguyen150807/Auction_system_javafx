package com.auction.client.service;

import com.auction.client.store.AuctionStore;
import com.auction.client.store.lotsinformation.ClientItem;
import com.auction.client.store.selectediteminformation.SelectedItem;
import com.auction.client.store.selectediteminformation.SelectedItemBidHistory;
import com.auction.client.util.RequestHelper;
import com.auction.shared.*;

import java.util.List;
import java.util.Map;

public class AuctionService {
  public static String registerLot(Map<String, String> lotForm) {
    return RequestHelper.sendRequest(Request.ADD_LOT, lotForm)
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK)) {
          return null;
        } else {
          return response.getMessage();
        }
      }).join();
  }

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
          AuctionStore.AUCTION_STORE.loadOngoingItems((List<Item>) ongoingLots);
          return (List<Item>) ongoingLots;
        } else {
          return null;
        }
      }).join();
  }

  public static List<BidTransaction> getSelectedItemBidHistory(int itemId) {
    return RequestHelper.sendRequest(Request.GET_BID_HISTORY, itemId)
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof List<?> bids) {
          return (List<BidTransaction>) bids;
        } else  {
          return null;
        }
      }).join();
  }

  public static void updateItem(Item item) {
    AuctionStore.AUCTION_STORE.updateClientItem(item);
    if (SelectedItemBidHistory.SELECTED_ITEM_BID_HISTORY.getSelectedItemId() == item.getId()) {
      BidTransaction bidTransaction = new BidTransaction(item.getId(), -1, item.getCurrentPrice());
      SelectedItemBidHistory.SELECTED_ITEM_BID_HISTORY.appendBidTransaction(bidTransaction);
    }
  }

  public static void setSelectedItem(ClientItem item) {
    SelectedItem.SELECTED_ITEM.setSelectedItem(item);
    SelectedItemBidHistory.SELECTED_ITEM_BID_HISTORY
      .setSelectedItem(item.getId(), getSelectedItemBidHistory(item.getId()));
  }
}
