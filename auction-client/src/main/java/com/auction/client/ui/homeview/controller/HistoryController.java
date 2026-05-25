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
import com.auction.client.ui.component.FilterBox;
import com.auction.client.ui.component.itemcard.ItemCardConfig;
import com.auction.client.ui.homeview.HomeViewType;
import com.auction.client.ui.homeview.homeviewcomponent.ReactiveFlowPane;
import com.auction.client.util.FXThread;
import com.auction.shared.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HistoryController implements CanRefresh, CanSwitchNode<HomeViewType> {
  @FXML private FilterBox filterBox;

  @FXML private ReactiveFlowPane<ItemModel> ongoingContainer;
  @FXML private ReactiveFlowPane<ItemModel> upcomingContainer;
  @FXML private ReactiveFlowPane<ItemModel> closedContainer;
  @FXML private ReactiveFlowPane<ItemModel> pastContainer;

  private final AuctionDiscoveryService discoveryService;
  private AuctionDetailService detailService;
  private ClientService clientService;

  private final ObservableList<ItemModel> pastRawList = FXCollections.observableArrayList();

  private FilteredList<ItemModel> filteredClosed;
  private FilteredList<ItemModel> filteredUpcoming;
  private FilteredList<ItemModel> filteredOngoing;
  private FilteredList<ItemModel> filteredPast;

  private Consumer<HomeViewType> switchNode;

  private final Consumer<ItemModel> onCardClicked = (selectedItem) -> {
    detailService.setSelectedItem(selectedItem);
    switchNode.accept(HomeViewType.ITEM_INFORMATION);
  };

  private final Consumer<ItemModel> onHeartClicked = (selectedItem) -> {
    int itemId = selectedItem.getId();
    boolean isWatching = ClientSession.CURRENT_SESSION.getWatchedItemsList().contain(itemId);
    clientService.toggleWatchedItem(itemId, !isWatching);
  };

  private final ItemCardConfig itemCardConfig = new ItemCardConfig(onHeartClicked, onCardClicked);

  @AutoInject
  public HistoryController(AuctionDiscoveryService discoveryService, AuctionDetailService detailService, ClientService clientService) {
    this.discoveryService = discoveryService;
    this.detailService = detailService;
    this.clientService = clientService;
  }

  @FXML
  private void initialize() {
    initFilteredLists();
    bindContainersToUi();

    filterBox.registerListsToFilter(List.of(filteredClosed, filteredUpcoming, filteredOngoing, filteredPast));
  }

  private void initFilteredLists() {
    filteredClosed = new FilteredList<>(ClosedLots.CLOSED_LOTS.getClosedLots());
    filteredUpcoming = new FilteredList<>(OpenLots.AUCTION_STORE.getUpcomingClientItemList());
    filteredOngoing = new FilteredList<>(OpenLots.AUCTION_STORE.getOngoingClientItemList());
    filteredPast = new FilteredList<>(pastRawList);
  }

  private void bindContainersToUi() {
    closedContainer.bind(filteredClosed, itemCardConfig.cardFactory());
    upcomingContainer.bind(filteredUpcoming, itemCardConfig.cardFactory());
    ongoingContainer.bind(filteredOngoing, itemCardConfig.cardFactory());
    pastContainer.bind(filteredPast, itemCardConfig.cardFactory());
  }

  @Override
  public void refreshData() {
    discoveryService.refreshOngoingLots();
    discoveryService.refreshClosedLots();
    discoveryService.getTrendingLots()
      .thenAccept(list -> {
        if (list == null) return;

        List<ItemModel> items = new ArrayList<>();
        for (Item item : list) {
          if (item != null) {
            items.add(new ItemModel(item));
          }
        }
        FXThread.run(() -> {
          pastRawList.setAll(items);
        });
      })
      .exceptionally(ex -> {
        ex.printStackTrace();
        return null;
      });
    filterBox.executeInternalFilter();
  }

  @Override
  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
  }
}