package com.auction.client.ui.homeview.controller;

import com.auction.client.app.AutoInject;
import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.service.user.ClientService;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.lotsinformation.ItemModelRegistry;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.component.FilterBox;
import com.auction.client.ui.component.MetricCard;
import com.auction.client.ui.component.itemcard.ItemCardConfig;
import com.auction.client.ui.homeview.HomeViewType;
import com.auction.client.ui.homeview.homeviewcomponent.ReactiveFlowPane;
import com.auction.client.util.FXThread;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class YourItemController implements CanRefresh, CanSwitchNode<HomeViewType> {
  @FXML private MetricCard activeItemCard;
  @FXML private MetricCard inventoryValue;
  @FXML private ReactiveFlowPane<ItemModel> inventoryPane;
  @FXML private FilterBox filterBox;

  private Consumer<HomeViewType> switchNode;

  private ClientService clientService;
  private AuctionDetailService detailService;

  private final ObservableList<ItemModel> rawUserItems = FXCollections.observableArrayList();
  private final FilteredList<ItemModel> filteredUserItems = new FilteredList<>(rawUserItems);

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
  public YourItemController(ClientService clientService, AuctionDetailService detailService) {
    this.clientService = clientService;
    this.detailService = detailService;
  }

  @FXML
  public void initialize() {
    inventoryPane.bind(filteredUserItems, itemCardConfig.cardFactory());
    filterBox.registerListsToFilter(List.of(filteredUserItems));
    filterBox.setOnApply(this::calculateMetricsFromDisplayedItems);
    refreshData();
  }

  @FXML
  private void handleRefresh() {
    refreshData();
  }

  @Override
  public void refreshData() {
    clientService.getUserItem()
      .thenAccept(list -> {
        if (list == null) return;

        List<ItemModel> securedModels = new ArrayList<>();
        for (Item item : list) {
          if (item != null) {
            ItemModelRegistry.updateIfNewer(item);
            securedModels.add(ItemModelRegistry.getOrCreate(item));
          }
        }

        FXThread.run(() -> {
          rawUserItems.setAll(securedModels);
          filterBox.executeInternalFilter();
          calculateMetricsFromDisplayedItems();
        });
      });
  }

  private void calculateMetricsFromDisplayedItems() {
    double totalIncome = 0;
    int totalActive = 0;

    for (ItemModel itemModel : filteredUserItems) {
      Item item = itemModel.getItem();
      if (item == null) continue;

      if (item.getWinnerId() != 0) {
        totalIncome += item.getCurrentPrice();
      }

      if (ItemStatus.OPEN.equals(item.getStatus())) {
        totalActive++;
      }
    }

    double finalIncome = totalIncome;
    int finalActive = totalActive;

    FXThread.run(() -> {
      activeItemCard.setText(String.valueOf(finalActive));
      inventoryValue.setText(String.format("%,.0f $", finalIncome));
    });
  }

  @Override
  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
  }
}