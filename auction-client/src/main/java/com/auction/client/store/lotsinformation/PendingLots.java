package com.auction.client.store.lotsinformation;

import com.auction.client.util.FXThread;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PendingLots {
  public static PendingLots PENDING_LOTS = new PendingLots();

  private final ObservableList<ItemModel> pendingItemsList = FXCollections.observableArrayList();

  private final Map<Integer, ItemModel> itemModelMap = new HashMap<>();

  public ObservableList<ItemModel> getPendingItemsList() {
    return pendingItemsList;
  }

  public void loadPendingItems(List<Item> items) {
    FXThread.run(() -> {
      for (Item item : items) {
        addItemIfMissing(item);
        updateClientItem(item);
      }
    });
  }

  public void removeClientItem(Item item) {
    FXThread.run(() -> {
      if (itemModelMap.containsKey(item.getId())) {
        ItemModel itemModel = itemModelMap.get(item.getId());
        itemModelMap.remove(item.getId());
        pendingItemsList.remove(itemModel);
      }
    });
  }

  public void updateClientItem(Item item) {
    FXThread.run(() -> {
      ItemModel clientItem = itemModelMap.get(item.getId());
      if (clientItem != null) {
        clientItem.update(item);
        if (item.getStatus().equals(ItemStatus.CLOSED)) {
          pendingItemsList.remove(clientItem);
        }
      } else {
        addItemIfMissing(item);
      }
    });
  }

  public void addItemIfMissing(Item item) {
    FXThread.run(() -> {
      ItemModel clientItem = itemModelMap.get(item.getId());
      if (clientItem == null) {
        clientItem = new ItemModel(item);
        pendingItemsList.add(clientItem);
        itemModelMap.put(item.getId(), clientItem);
      }
    });
  }
}
