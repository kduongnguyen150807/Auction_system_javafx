package com.auction.client.store.selectediteminformation;

import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.lotsinformation.ItemModelRegistry;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class SelectedItem {
  public static final SelectedItem SELECTED_ITEM = new SelectedItem();

  private final ObjectProperty<ItemModel> selectedItem = new SimpleObjectProperty<>();

  private SelectedItem() {
  }

  public void setSelectedItem(ItemModel item) {
    if (item == null || item.getItem() == null) {
      selectedItem.set(null);
      return;
    }
    ItemModel singleItem = ItemModelRegistry.getOrCreate(item.getItem());
    selectedItem.set(singleItem);
  }

  public ItemModel getSelectedItem() {
    return selectedItem.get();
  }

  public ObjectProperty<ItemModel> selectedItemProperty() {
    return selectedItem;
  }
}
