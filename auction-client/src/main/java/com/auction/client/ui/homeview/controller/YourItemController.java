package com.auction.client.ui.homeview.controller;

import com.auction.client.app.AutoInject;
import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.service.user.ClientService;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.component.MetricCard;
import com.auction.client.ui.component.itemcard.ItemCardConfig;
import com.auction.client.ui.homeview.HomeViewType;
import com.auction.client.ui.homeview.homeviewcomponent.ReactiveFlowPane;
import com.auction.client.util.FXThread;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import java.util.function.Consumer;

public class YourItemController implements CanRefresh, CanSwitchNode<HomeViewType> {
  @FXML private MetricCard activeItemCard;
  @FXML private MetricCard inventoryValue;
  @FXML private ReactiveFlowPane<ItemModel> inventoryPane;

  private Consumer<HomeViewType> switchNode;

  private ClientService clientService;
  private AuctionDetailService detailService;

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
  public YourItemController(ClientService clientService, AuctionDetailService detailService) {
    this.clientService = clientService;
    this.detailService = detailService;
  }

  @FXML
  private void handleRefresh() {
    refreshData();
  }

  @Override
  public void refreshData() {
    clientService.getUserItem()
      .thenCompose(list -> {
        FXThread.run(() -> {
          ObservableList<ItemModel> items = FXCollections.observableArrayList();
          double income = 0;
          int active = 0;

          for (Item item: list) {
            items.add(new ItemModel(item));

            if (item.getWinnerId() != 0) {
              income = income + item.getCurrentPrice();
            }

            if (item.getStatus().equals(ItemStatus.OPEN)) {
              active = active + 1;
            }
          }

          activeItemCard.setText(String.valueOf(active));
          inventoryValue.setText(String.valueOf(income));
          inventoryPane.bind(items, itemCardConfig.cardFactory());
        });
        return null;
      });
  }

  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
  }
}
