package com.auction.client.ui.homeview.controller;

import com.auction.client.app.AutoInject;
import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.service.auction.AuctionDiscoveryService;
import com.auction.client.service.user.ClientService;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.lotsinformation.ClosedLots;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.lotsinformation.OpenLots;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.component.itemcard.ItemCardConfig;
import com.auction.client.ui.homeview.HomeViewType;
import com.auction.client.ui.homeview.homeviewcomponent.ReactiveFlowPane;
import com.auction.client.util.FXThread;
import com.auction.shared.Item;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class HistoryController implements CanRefresh, CanSwitchNode<HomeViewType> {
  @FXML private ReactiveFlowPane<ItemModel> ongoingContainer;
  @FXML private ReactiveFlowPane<ItemModel> upcomingContainer;
  @FXML private ReactiveFlowPane<ItemModel> closedContainer;
  @FXML private ReactiveFlowPane<ItemModel> pastContainer;

  private final AuctionDiscoveryService discoveryService;
  private AuctionDetailService detailService;
  private ClientService clientService;

  private Consumer<HomeViewType> switchNode;
  Consumer<ItemModel> onCardClicked = (selectedItem) -> {
    detailService.setSelectedItem(selectedItem);
    switchNode.accept(HomeViewType.ITEM_INFORMATION);
  };
  Consumer<ItemModel> onHeartClicked = (selectedItem) -> {
    int itemId = selectedItem.getId();
    boolean isWatching = ClientSession.CURRENT_SESSION.getWatchedItemsList().contain(itemId);
    clientService.toggleWatchedItem(itemId, !isWatching);
  };
  ItemCardConfig itemCardConfig = new ItemCardConfig(onHeartClicked, onCardClicked);

  @AutoInject
  public HistoryController(AuctionDiscoveryService discoveryService,  AuctionDetailService detailService,  ClientService clientService) {
    this.discoveryService = discoveryService;
    this.detailService = detailService;
    this.clientService = clientService;
  }

  @FXML
  private void initialize() {
    bind();
  }

  private void bind() {
    closedContainer.bind(ClosedLots.CLOSED_LOTS.getClosedLots(), itemCardConfig.cardFactory());
    upcomingContainer.bind(OpenLots.AUCTION_STORE.getUpcomingClientItemList(),  itemCardConfig.cardFactory());
    ongoingContainer.bind(OpenLots.AUCTION_STORE.getOngoingClientItemList(), itemCardConfig.cardFactory());
  }

  @Override
  public void refreshData() {
    discoveryService.refreshOngoingLots();
    discoveryService.refreshClosedLots();
    discoveryService.getTrendingLots()
      .thenCompose(list -> {
        FXThread.run(() -> {
          List<ItemModel> items = new ArrayList<>();
          for (Item item : list) {
            items.add(new ItemModel(item));
          }
          pastContainer.getChildren().clear();
          pastContainer.setAll(items, itemCardConfig.cardFactory());
        });
        return null;
      });
  }

  @Override
  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
  }
}