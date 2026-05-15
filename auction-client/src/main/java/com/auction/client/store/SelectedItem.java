package com.auction.client.store;

import com.auction.shared.Item;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class SelectedItem {
  public static final SelectedItem SELECTED_ITEM = new SelectedItem();

  private final ObjectProperty<Item> selectedItem = new SimpleObjectProperty<>();

  public Item getSelectedItem() {
    return selectedItem.get();
  }

  public void setSelectedItem(Item item) {
    selectedItem.set(item);
  }

  public ObjectProperty<Item> selectedItemProperty() {
    return selectedItem;
  }
}
