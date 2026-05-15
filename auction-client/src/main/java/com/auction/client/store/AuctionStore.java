package com.auction.client.store;

import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;
import java.util.function.Predicate;

public class AuctionStore {
  public static AuctionStore AUCTION_STORE = new AuctionStore();

  private final ObservableList<Item> items = FXCollections.observableArrayList();

  public FilteredList<Item> filterStatus(ItemStatus status) {
    return filter(item -> item.getStatus().equals(status));
  }

  public FilteredList<Item> filterCategory(String category) {
    return filter(item -> item.getCategory().equalsIgnoreCase(category));
  }

  public ObservableList<Item> getItems() {
    return items;
  }

  public void refreshItems(List<Item> items) {
    this.items.clear();
    this.items.setAll(items);
  }

  public FilteredList<Item> filter(Predicate<Item> predicate) {
    return new FilteredList<Item>(this.items, predicate);
  }
}
