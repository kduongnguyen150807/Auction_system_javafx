package com.auction.client.store.selectediteminformation;

import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.lotsinformation.ItemModelRegistry;
import com.auction.client.util.FXThread;
import com.auction.shared.ItemStatus;
import com.auction.shared.Rating;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import java.util.List;

public class SelectedItem {
  public static final SelectedItem SELECTED_ITEM = new SelectedItem();

  private final ObjectProperty<ItemModel> selectedItem = new SimpleObjectProperty<>();

  private final ObservableList<Rating> selectedItemRatings = FXCollections.observableArrayList();

  private SelectedItem() {
  }

  public void setSelectedItem(ItemModel item) {
    if (item == null || item.getItem() == null) {
      selectedItem.set(null);
      return;
    }

    if (item.getStatus().equals(ItemStatus.OPEN)) {
      ItemModel singleItem = ItemModelRegistry.getOrCreate(item.getItem());
      selectedItem.set(singleItem);
    }

    selectedItem.set(item);
  }

  public void setSelectedItemRatings(List<Rating> ratings) {
    FXThread.run(() -> {
      selectedItemRatings.clear();
      selectedItemRatings.addAll(ratings);
    });
  }

  public ObservableList<Rating> getSelectedItemRatings() {
    return selectedItemRatings;
  }

  public ItemModel getSelectedItem() {
    return selectedItem.get();
  }

  public ObjectProperty<ItemModel> selectedItemProperty() {
    return selectedItem;
  }
}
