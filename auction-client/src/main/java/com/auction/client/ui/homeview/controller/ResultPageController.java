package com.auction.client.ui.homeview.controller;

import com.auction.client.store.ClientItem;
import com.auction.client.store.ResultStore;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.homeview.HomeViewType;
import com.auction.client.ui.homeview.homeviewcomponent.ItemCard;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;

import java.util.function.Consumer;

public class ResultPageController implements CanSwitchNode<HomeViewType> {
  @FXML private FlowPane itemContainer;

  private Consumer<HomeViewType> switchNode;

  private final FilteredList<ClientItem> filteredItems = ResultStore.RESULT_STORE.getClientItems();

  @FXML
  public void initialize() {
    renderItems();
    filteredItems.addListener((ListChangeListener<? super ClientItem>)  change -> {
      renderItems();
    });
  }

  private void renderItems() {
    itemContainer.getChildren().clear();
    for (ClientItem item : filteredItems) {
      ItemCard itemCard = new ItemCard(item);
      itemContainer.getChildren().add(itemCard);
    }
  }

  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
  }
}
