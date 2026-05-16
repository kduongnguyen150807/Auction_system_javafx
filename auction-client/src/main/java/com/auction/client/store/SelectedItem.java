package com.auction.client.store;

import com.auction.shared.Item;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class SelectedItem {
  public static final SelectedItem SELECTED_ITEM = new SelectedItem();

  private final ObjectProperty<ClientItem> selectedItem = new SimpleObjectProperty<>();

  public void setSelectedItem(ClientItem item) {
    selectedItem.set(item);
  }

  public ClientItem getSelectedItem() {
    return selectedItem.get();
  }

  public ObjectProperty<ClientItem> selectedItemProperty() {
    return selectedItem;
  }
}
