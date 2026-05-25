package com.auction.client.store.lotsinformation;

import com.auction.client.util.FXThread;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

public class OpenLots {
  public static final OpenLots AUCTION_STORE = new OpenLots();

  private final ObservableList<ItemModel> ongoingItemsList = FXCollections.observableArrayList();
  private final ObservableList<ItemModel> upcomingItemsList = FXCollections.observableArrayList();

  private OpenLots() {}

  public ObservableList<ItemModel> getOngoingClientItemList() {
    return ongoingItemsList;
  }

  public ObservableList<ItemModel> getUpcomingClientItemList() {
    return upcomingItemsList;
  }

  public void loadOngoingItems(List<Item> items) {
    if (items == null) return;

    FXThread.run(() -> {
      for (Item item : items) {
        if (item != null) {
          updateClientItem(item);
        }
      }
    });
  }

  public void updateClientItem(Item item) {
    FXThread.run(() -> {

      ItemModelRegistry.updateIfNewer(item);
      ItemModel securedModel = ItemModelRegistry.getOrCreate(item);

      if (item.getStatus().equals(ItemStatus.CLOSED) || item.getStatus().equals(ItemStatus.EXPIRED)) {
        ongoingItemsList.remove(securedModel);
        upcomingItemsList.remove(securedModel);
      } else if (item.getStartTime().isAfter(LocalDateTime.now())) {
        if (!upcomingItemsList.contains(securedModel)) {
          upcomingItemsList.add(securedModel);
        }
        ongoingItemsList.remove(securedModel);
      } else {
        if (!ongoingItemsList.contains(securedModel)) {
          ongoingItemsList.add(securedModel);
        }
        upcomingItemsList.remove(securedModel);
      }
    });
  }

  public FilteredList<ItemModel> getFilteredStatusItems(ItemStatus status) {
    return getFilteredItems(clientItem -> clientItem.getStatus().equals(status));
  }

  public FilteredList<ItemModel> getFilteredItems(Predicate<ItemModel> predicate) {
    return new FilteredList<>(ongoingItemsList, predicate);
  }

  public void clear() {
    FXThread.run(() -> {
      ongoingItemsList.clear();
      upcomingItemsList.clear();
    });
  }
}