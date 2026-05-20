package com.auction.client.store.selectediteminformation;

import com.auction.client.store.lotsinformation.ItemModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class SelectedItem {
  public static final SelectedItem SELECTED_ITEM = new SelectedItem();

  private final ObjectProperty<ItemModel> selectedItem = new SimpleObjectProperty<>();

  public void setSelectedItem(ItemModel item) {
    selectedItem.set(item);
  }

  public ItemModel getSelectedItem() {
    return selectedItem.get();
  }

  public ObjectProperty<ItemModel> selectedItemProperty() {
    return selectedItem;
  }
}
