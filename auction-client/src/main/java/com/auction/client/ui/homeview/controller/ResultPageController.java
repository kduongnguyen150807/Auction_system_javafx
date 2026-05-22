package com.auction.client.ui.homeview.controller;

import com.auction.client.app.AutoInject;
import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.lotsinformation.ResultStore;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.homeview.HomeViewType;
import com.auction.client.ui.homeview.homeviewcomponent.ItemCard;
import com.auction.client.ui.homeview.homeviewcomponent.ReactiveFlowPane;
import com.auction.client.util.FXThread; // Helper bọc Platform.runLater an toàn
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ResultPageController implements CanSwitchNode<HomeViewType> {
  @FXML private ReactiveFlowPane itemContainer;

  private Consumer<HomeViewType> switchNode;
  private final FilteredList<ItemModel> filteredItems = ResultStore.RESULT_STORE.getClientItems();

  private final AuctionDetailService detailService;

  private ToggleGroup toggleGroup = new ToggleGroup();

  @AutoInject
  public ResultPageController(AuctionDetailService auctionDetailService) {
    this.detailService = auctionDetailService;
  }

  @FXML
  public void initialize() {
    bind();
  }

  private void bind() {
    Consumer<ItemModel> onCardClicked = (selectedItem) -> {
      detailService.setSelectedItem(selectedItem);
      switchNode.accept(HomeViewType.ITEM_INFORMATION);
    };
    itemContainer.bindList(ResultStore.RESULT_STORE.getClientItems(), onCardClicked);
  }

  @Override
  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
  }
}