package com.auction.client.ui.homeview.controller;

import com.auction.client.app.AutoInject;
import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.service.auction.AuctionDiscoveryService;
import com.auction.client.store.lotsinformation.ClosedLots;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.lotsinformation.OngoingLots;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.homeview.HomeViewType;
import com.auction.client.ui.homeview.homeviewcomponent.ReactiveFlowPane;
import javafx.fxml.FXML;

import java.util.function.Consumer;


public class HistoryController implements CanRefresh, CanSwitchNode<HomeViewType> {
  @FXML private ReactiveFlowPane ongoingContainer;
  @FXML private ReactiveFlowPane upcomingContainer;
  @FXML private ReactiveFlowPane closedContainer;
  @FXML private ReactiveFlowPane pastContainer;

  private final AuctionDiscoveryService discoveryService;
  private final AuctionDetailService detailService;

  private Consumer<HomeViewType> switchNode;

  @AutoInject
  public HistoryController(AuctionDiscoveryService discoveryService,  AuctionDetailService detailService) {
    this.discoveryService = discoveryService;
    this.detailService = detailService;
  }

  @FXML
  private void initialize() {
    bind();
  }

  private void bind() {
    Consumer<ItemModel> onCardClicked = (selectedItem) -> {
      detailService.setSelectedItem(selectedItem);
      switchNode.accept(HomeViewType.ITEM_INFORMATION);
    };

    ongoingContainer.bindList(OngoingLots.AUCTION_STORE.getOngoingClientItemList(), onCardClicked);
    closedContainer.bindList(ClosedLots.CLOSED_LOTS.getClosedLots(), onCardClicked);
  }

  @Override
  public void refreshData() {
    discoveryService.refreshOngoingLots();
    discoveryService.refreshClosedLots();
  }

  @Override
  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
  }
}