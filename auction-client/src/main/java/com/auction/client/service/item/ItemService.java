package com.auction.client.service.item;

import com.auction.client.store.lotsinformation.PendingLots;
import com.auction.client.util.RequestHelper;
import com.auction.shared.Item;
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
}