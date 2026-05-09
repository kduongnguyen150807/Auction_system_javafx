package com.auction.client.store;

import com.auction.shared.item.Item;
import com.auction.shared.item.ItemStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class AuctionStore {
  private final ObservableList<Item> items = FXCollections.observableArrayList();

  public ObservableList<Item> filterStatus(ItemStatus status) {
    List<Item> list = items.stream().filter(item -> item.getStatus().equals(status)).toList();
    return FXCollections.observableArrayList(list);
  }

  public void refreshItem(List<Item> items) {
    this.items.clear();
    this.items.addAll(items);
  }
}
