package com.auction.client.service.auction;

import com.auction.client.store.lotsinformation.OngoingLots;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.selectediteminformation.SelectedItem;
import com.auction.client.store.selectediteminformation.SelectedItemBidHistory;
import com.auction.client.util.RequestHelper;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.List;

public class AuctionDetailService {

  public List<BidTransaction> getSelectedItemBidHistory(int itemId) {
    return RequestHelper.sendRequest(Request.GET_BID_HISTORY, itemId)
      .thenApply(response -> {
        if (response.getStatus().equals(Response.OK) && response.getPayload() instanceof List<?> bids) {
          return (List<BidTransaction>) bids;
        }
        return null;
      }).join();
  }

  public void setSelectedItem(ItemModel item) {
    SelectedItem.SELECTED_ITEM.setSelectedItem(item);
    SelectedItemBidHistory.SELECTED_ITEM_BID_HISTORY
      .setSelectedItem(item.getId(), getSelectedItemBidHistory(item.getId()));
  }

  public void handleRealtimeItemUpdate(Item updatedItem) {
    OngoingLots.AUCTION_STORE.updateClientItem(updatedItem);
    if (SelectedItemBidHistory.SELECTED_ITEM_BID_HISTORY.getSelectedItemId() == updatedItem.getId()) {
      BidTransaction fakeTransaction = new BidTransaction(updatedItem.getId(), -1, updatedItem.getCurrentPrice());
      SelectedItemBidHistory.SELECTED_ITEM_BID_HISTORY.appendBidTransaction(fakeTransaction);
    }
  }
}