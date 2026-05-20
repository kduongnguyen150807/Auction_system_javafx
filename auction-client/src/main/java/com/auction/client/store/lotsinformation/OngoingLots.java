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

public class OngoingLots {
  public static OngoingLots AUCTION_STORE = new OngoingLots();

  private final ObservableList<ItemModel> ongoingItemsList = FXCollections.observableArrayList();

  private final Map<Integer, ItemModel> clientItemMap = new HashMap<>();

  public ObservableList<ItemModel> getOngoingClientItemList() {
    return ongoingItemsList;
  }

  public void loadOngoingItems(List<Item> items) {
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
        if (item.getStatus().equals(ItemStatus.CLOSED)) {
          ongoingItemsList.remove(clientItem);
        }
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
        ongoingItemsList.add(clientItem);
        clientItemMap.put(item.getId(), clientItem);
      }
    });
  }

  public FilteredList<ItemModel> getFilteredStatusItems(ItemStatus status) {
    return getFilteredItems(clientItem -> clientItem.getStatus().equals(status));
  }

  public FilteredList<ItemModel> getFilteredItems(Predicate<ItemModel> predicate) {
    return new FilteredList<>(ongoingItemsList, predicate);
  }
}
