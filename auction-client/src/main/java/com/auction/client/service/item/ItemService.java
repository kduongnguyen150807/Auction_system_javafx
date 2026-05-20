package com.auction.client.service.item;

import com.auction.client.store.lotsinformation.PendingLots;
import com.auction.client.util.RequestHelper;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;

import java.util.List;

public class ItemService {
  public boolean loadPendingItem() {
    return RequestHelper.sendRequest(Request.GET_PENDING_ITEMS, null)
      .thenApply(response -> {
        if (Response.OK.equals(response.getStatus()) && (response.getPayload() instanceof List<?> li)) {
          PendingLots.PENDING_LOTS.loadPendingItems((List<Item>) li);
          return true;
        } else {
          return false;
        }
      }).join();
  }
}
