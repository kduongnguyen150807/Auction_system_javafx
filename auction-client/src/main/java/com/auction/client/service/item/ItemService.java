package com.auction.client.service.item;

import com.auction.client.store.lotsinformation.PendingLots;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.RequestHelper;
import com.auction.shared.Item;
import com.auction.shared.Rating;
import com.auction.shared.Request;
import com.auction.shared.Response;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ItemService {

  @SuppressWarnings("unchecked")
  public CompletableFuture<Void> loadPendingItems() {
    return RequestHelper.sendRequest(Request.GET_PENDING_ITEMS, null)
      .thenAccept(response -> {
        if (Response.OK.equals(response.getStatus()) && (response.getPayload() instanceof List<?> rawList)) {
          List<Item> pendingList = (List<Item>) rawList;
          PendingLots.PENDING_LOTS.loadPendingItems(pendingList);
        } else {
          throw new RuntimeException(response.getMessage() != null ? response.getMessage() : "Failed to load pending items");
        }
      });
  }

  public CompletableFuture<List<Rating>> loadRatings(int id) {
    return RequestHelper.sendRequest(Request.GET_RATINGS, id)
      .thenApply(response ->  {
        if (Response.OK.equals(response.getStatus()) && (response.getPayload() instanceof List<?> rawList)) {
          List<Rating> ratings = (List<Rating>) rawList;
          return ratings;
        } else {
          return List.of();
        }
      });
  }

  public CompletableFuture<Void> submitRating(Rating rating) {
    return RequestHelper.sendRequest(Request.SUBMIT_RATING, rating)
      .thenAccept(response -> {
        if (Response.OK.equals(response.getStatus())) {
          AlertUtil.showInfoAlert("RATING", "RATING SUBMITTED");
        } else {
          AlertUtil.showErrorAlert("RATING", response.getMessage());
        }
      });
  }
}