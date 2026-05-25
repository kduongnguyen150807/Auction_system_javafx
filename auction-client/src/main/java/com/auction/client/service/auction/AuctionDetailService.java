package com.auction.client.service.auction;

import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.lotsinformation.OpenLots;
import com.auction.client.store.selectediteminformation.SelectedItem;
import com.auction.client.store.selectediteminformation.SelectedItemBidHistory;
import com.auction.client.store.userinformation.SelectedUser;
import com.auction.client.store.userinformation.UserModel;
import com.auction.client.util.RequestHelper;
import com.auction.shared.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AuctionDetailService {

  public CompletableFuture<List<BidTransaction>> getSelectedItemBidHistory(int itemId) {
    return RequestHelper.sendRequest(Request.GET_BID_HISTORY, itemId)
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof List<?> bids) {
          return (List<BidTransaction>) bids;
        }
        return List.of();
      });
  }

  public void setSelectedItem(ItemModel item) {
    if (item == null) return;
    SelectedItem.SELECTED_ITEM.setSelectedItem(item);
    getSelectedItemBidHistory(item.getId())
      .thenAccept(bidHistory -> {
        SelectedItemBidHistory.SELECTED_ITEM_BID_HISTORY
          .setSelectedItem(item.getId(), bidHistory);
      });
  }

  public void handleRealtimeItemUpdate(Item updatedItem) {
    if (updatedItem == null) return;
    OpenLots.AUCTION_STORE.updateClientItem(updatedItem);
    if (SelectedItemBidHistory.SELECTED_ITEM_BID_HISTORY.getSelectedItemId() == updatedItem.getId()) {
      BidTransaction fakeTransaction = new BidTransaction(updatedItem.getId(), -1, updatedItem.getCurrentPrice());
      SelectedItemBidHistory.SELECTED_ITEM_BID_HISTORY.appendBidTransaction(fakeTransaction);
    }
  }

  public void closeItem(Item item) {
    if (item == null) return;
    OpenLots.AUCTION_STORE.updateClientItem(item);
  }

  public CompletableFuture<Void> loadRating(int id) {
    return RequestHelper.sendRequest(Request.GET_RATINGS, id)
      .thenAccept(response -> {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof List<?> ratings) {
          if (SelectedItem.SELECTED_ITEM.getSelectedItem().getId() == id) {
            SelectedItem.SELECTED_ITEM.setSelectedItemRatings((List<Rating>) ratings);
          }
        }
      });
  }

  public CompletableFuture<List<Item>> getSelectedUserItems(int id) {
    return RequestHelper.sendRequest(Request.GET_MY_ITEMS, id)
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof List<?> items) {
          return (List<Item>) items;
        } else {
          return List.of();
        }
      });
  }

  public void setSelectedUser(UserModel user) {
    if (user == null) return;
    SelectedUser.SELECTED_USER.setSelectedUser(user);
  }
}