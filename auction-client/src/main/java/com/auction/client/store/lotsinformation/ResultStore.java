package com.auction.client.store.lotsinformation;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;

public class ResultStore {
  public static ResultStore RESULT_STORE = new ResultStore();

  private ResultStore() {
  }

  private final ObservableList<ItemModel> resultItem = OngoingLots.AUCTION_STORE.getOngoingClientItemList();

  private final FilteredList<ItemModel> filteredItems = new FilteredList<>(resultItem);

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

  public FilteredList<ItemModel> getClientItems() {
    return filteredItems;
  }

  public void clearFilter() {
    filteredItems.clear();
  }
}
