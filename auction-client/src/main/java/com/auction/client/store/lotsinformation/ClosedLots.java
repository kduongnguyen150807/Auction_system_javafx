package com.auction.client.store.lotsinformation;

import com.auction.client.util.FXThread;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ClosedLots {
  public static ClosedLots CLOSED_LOTS = new ClosedLots();

  private final ObservableList<ItemModel> closedList = FXCollections.observableArrayList();

  private final Map<Integer, ItemModel> clientItemMap = new HashMap<>();

  public ObservableList<ItemModel> getClosedLots() {
    return closedList;
  }

  public void setClosedLots(List<Item> items) {
    FXThread.run(() -> {
      for (Item item : items) {
        addItemIfMissing(item);
        updateClientItem(item);
      }
    });
  }

  public void updateClientItem(Item item) {
    FXThread.run(() -> {
      ItemModel clientItem = clientItemMap.get(item.getId());
      if (clientItem != null) {
        clientItem.update(item);
      } else {
        addItemIfMissing(item);
      }
    });
  }

  public void addItemIfMissing(Item item) {
    FXThread.run(() -> {
      ItemModel clientItem = clientItemMap.get(item.getId());
      if (clientItem == null) {
        clientItem = new ItemModel(item);
        closedList.add(clientItem);
        clientItemMap.put(item.getId(), clientItem);
      }
    });
  }

  public FilteredList<ItemModel> getFilteredStatusItems(ItemStatus status) {
    return getFilteredItems(clientItem -> clientItem.getStatus().equals(status));
  }

  public FilteredList<ItemModel> getFilteredItems(Predicate<ItemModel> predicate) {
    return new FilteredList<>(closedList, predicate);
  }

  public void clear() {
    FXThread.run(() -> {
      closedList.clear();
      clientItemMap.clear();
    });
  }
}