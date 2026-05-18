package com.auction.client.store;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;

public class ResultStore {
  public static ResultStore RESULT_STORE = new ResultStore();

  private ResultStore() {
  }

  private final ObservableList<ClientItem> resultItem = AuctionStore.AUCTION_STORE.getOngoingClientItemList();

  private final FilteredList<ClientItem> filteredItems = new FilteredList<>(resultItem);

  public void filterWords(List<String> words) {
    filteredItems.setPredicate(clientItem -> {
      String name = clientItem.getItem().getName().toLowerCase();
      for (String word : words) {
        String normalized = word.toLowerCase().trim();
        if (!name.contains(normalized)) {
          return false;
        }
      }
      return true;
    });
  }

  public FilteredList<ClientItem> getClientItems() {
    return filteredItems;
  }

  public void clearFilter() {
    filteredItems.clear();
  }
}
