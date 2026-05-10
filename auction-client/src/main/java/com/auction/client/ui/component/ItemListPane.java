package com.auction.client.ui.component;

import com.auction.shared.item.Item;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.layout.HBox;

public class ItemListPane extends HBox {
  private ObservableList<Item> items;

  public void setItems(ObservableList<Item> items) {
    this.items = items;
    System.out.println(items.size());
    renderItem();
    items.addListener((ListChangeListener<Item>) change -> {
      renderItem();
    });
  }

  private void renderItem() {
    getChildren().clear();

    for (Item item : items) {
      getChildren().add(new ItemCard(item));
    }
  }
}
