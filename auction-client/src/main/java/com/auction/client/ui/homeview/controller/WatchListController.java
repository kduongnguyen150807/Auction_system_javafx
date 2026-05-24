package com.auction.client.ui.homeview.controller;

import com.auction.client.app.AutoInject;
import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.service.user.ClientService;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.component.itemcard.ItemCardConfig;
import com.auction.client.ui.homeview.HomeViewType;
import com.auction.client.ui.homeview.homeviewcomponent.ReactiveFlowPane;
import com.auction.shared.Item;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WatchListController implements CanRefresh, CanSwitchNode<HomeViewType> {
  @FXML private ReactiveFlowPane<ItemModel> itemContainer;
  private Consumer<HomeViewType> switchNode;

  private AuctionDetailService detailService;
  private ClientService clientService;

  Consumer<ItemModel> onCardClicked = (selectedItem) -> {
    detailService.setSelectedItem(selectedItem);
    switchNode.accept(HomeViewType.ITEM_INFORMATION);
  };
  Consumer<ItemModel> onHeartClicked = (selectedItem) -> {
    int itemId = selectedItem.getId();
    boolean isWatching = ClientSession.CURRENT_SESSION.getWatchedItemsList().contain(itemId);
    clientService.toggleWatchedItem(itemId, !isWatching);
  };
  private final ItemCardConfig itemCardConfig = new ItemCardConfig(onHeartClicked, onCardClicked);

  @AutoInject
  public WatchListController(AuctionDetailService auctionDetailService, ClientService clientService) {
    this.detailService = auctionDetailService;
    this.clientService = clientService;
  }

  @FXML
  private void initialize() {
    refreshData();
  }

  @FXML
  private void refreshItems() {
    refreshData();
  }

  @Override
  public void refreshData() {
    clientService.getWatchedItems()
      .thenCompose(watchedItems -> {
        List<ItemModel> items = new ArrayList<>();
        for (Item item : watchedItems) {
          ItemModel itemModel = new ItemModel(item);
          items.add(itemModel);
        }

        if (!items.isEmpty()) {
          itemContainer.setAll(items, itemCardConfig.cardFactory());
        }

        return null;
      });
  }

  @Override
  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
  }
}
