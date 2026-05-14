package com.auction.client.store;

import com.auction.shared.item.Item;
import com.auction.shared.item.ItemStatus;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuctionStore {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuctionStore.class);

  private final ObservableList<Item> items = FXCollections.observableArrayList();

  private final ObjectProperty<Item> focusedItem = new SimpleObjectProperty<>();

  public Item getFocusedItem() {
    return focusedItem.get();
  }

  public void setFocusedItem(Item item) {
    focusedItem.set(item);
  }

  public ObjectProperty<Item> focusedItemProperty() {
    return focusedItem;
  }

  public ObservableList<Item> filterStatus(ItemStatus status) {
    List<Item> list = items.stream().filter(item -> item.getStatus().equals(status)).toList();
    LOGGER.info("Filtered list for status {}", status);
    return FXCollections.observableArrayList(list);
  }

  public ObservableList<Item> getItems() {
    return items;
  }

  public void refreshItem(List<Item> items) {
    this.items.clear();
    this.items.addAll(items);
  }
}
