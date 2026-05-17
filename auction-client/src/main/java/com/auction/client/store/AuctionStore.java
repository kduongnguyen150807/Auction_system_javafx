package com.auction.client.store;

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

public class AuctionStore {
  public static AuctionStore AUCTION_STORE = new AuctionStore();

  private final ObservableList<ClientItem> items = FXCollections.observableArrayList();

  private final Map<Integer, ClientItem> clientItemMap = new HashMap<>();

  public ObservableList<ClientItem> getClientItems() {
    return items;
  }

  public void loadItems(List<Item> items) {
    FXThread.run(() -> {
      for (Item item : items) {
        addItemIfMissing(item);
        updateClientItem(item);
      }
    });
  }

  public void updateClientItem(Item item) {
    FXThread.run(() -> {
      ClientItem clientItem = clientItemMap.get(item.getId());
      if (clientItem != null) {
        if (clientItem.getStatus().equals(ItemStatus.CLOSED)) {
          items.remove(clientItem);
          return;
        }
        clientItem.update(item);
      } else {
        addItemIfMissing(item);
      }
    });
  }

  public void addItemIfMissing(Item item) {
    FXThread.run(() -> {
      ClientItem clientItem = clientItemMap.get(item.getId());
      if (clientItem == null) {
        clientItem = new ClientItem(item);
        items.add(clientItem);
        clientItemMap.put(item.getId(), clientItem);
      }
    });
  }

  public FilteredList<ClientItem> getFilteredStatusItems(ItemStatus status) {
    return getFilteredItems(clientItem -> clientItem.getStatus().equals(status));
  }

  public FilteredList<ClientItem> getFilteredItems(Predicate<ClientItem> predicate) {
    return new FilteredList<>(items, predicate);
  }
}
