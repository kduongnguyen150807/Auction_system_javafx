package com.auction.client.ui.homeview.controller;

import com.auction.client.app.AutoInject;
import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.service.auction.AuctionDiscoveryService;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.lotsinformation.OngoingLots;
import com.auction.client.store.userinformation.UserModel;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.component.FilterBox;
import com.auction.client.ui.homeview.HomeViewType;
import com.auction.client.ui.homeview.homeviewcomponent.*;
import com.auction.client.util.FXThread;
import com.auction.client.util.TimelineUtils;
import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import javafx.animation.Timeline;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TrangChuController implements CanRefresh, CanSwitchNode<HomeViewType> {
  private Consumer<HomeViewType> switchNode;

  private final FilteredList<ItemModel> filteredList = new FilteredList<>(OngoingLots.AUCTION_STORE.getOngoingClientItemList());

  @FXML private RedOrBlueToolbar<AuctionType> auctionTypeToggle;
  private final ToggleGroup toggleGroup = new ToggleGroup();

  @FXML private VBoxModel<ItemModel, ItemCard> trendingBind;
  @FXML private VBoxModel<UserModel, UserBar> leaderboardContainer;
  @FXML private ListPane<ItemModel> artLots;
  @FXML private ListPane<ItemModel> electronicsLots;
  @FXML private ListPane<ItemModel> vehiclesLots;
  @FXML private FilterBox filterBox;

  private final AuctionDiscoveryService discoveryService;
  private AuctionDetailService detailService;

  private Timeline countdownTimeline;
  private final Consumer<ItemModel> onItemCardClicked = itemModel -> {
    if (detailService == null) return;
    detailService.setSelectedItem(itemModel);
    if (switchNode != null) {
      switchNode.accept(HomeViewType.ITEM_INFORMATION);
    }
  };

  @AutoInject
  public TrangChuController(AuctionDiscoveryService discoveryService, AuctionDetailService detailService) {
    this.discoveryService = discoveryService;
    this.detailService = detailService;
  }

  @FXML
  public void initialize() {
    filterBox.setOnApply(this::applyCombinedFilter);

    discoveryService.refreshOngoingLots();
    setToggleToolbar();
    initLotsRow();

    /* set up trendingbind */
    trendingBind.setItemFac(ItemCard::new);
    trendingBind.setOnItemClicked(onItemCardClicked);
    refreshTrendingBid();

    /* set up leaderboard*/
    leaderboardContainer.setItemFac(UserBar::new);
    refreshLeaderBoard();
    countdownTimeline = TimelineUtils.setTimeline(countdownTimeline, 30, List.of(this::refreshLeaderBoard, this::refreshTrendingBid));
  }

  private void setToggleToolbar() {
    auctionTypeToggle.setUpToggleGroup(toggleGroup);
    auctionTypeToggle.setData("Browse category", AuctionType.ENGLISH, AuctionType.DUTCH);
    toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null) {
        if (oldValue != null) oldValue.setSelected(true);
        return;
      }
      applyCombinedFilter();
    });
    applyCombinedFilter();
  }

  private void initLotsRow() {
    registerLotsRow("ARTS LOTS", "ART", artLots);
    registerLotsRow("VEHICLES LOTS", "VEHICLE", vehiclesLots);
    registerLotsRow("ELECTRONICS LOTS", "ELECTRONICS", electronicsLots);
  }

  private void applyCombinedFilter() {
    if (toggleGroup.getSelectedToggle() == null) return;
    AuctionType selectedAuctionType = (AuctionType) toggleGroup.getSelectedToggle().getUserData();
    int minPrice = filterBox.getMinPrice();
    int maxPrice = filterBox.getMaxPrice();
    String targetCategory = filterBox.getCategory();
    FXThread.run(() -> {
      filteredList.setPredicate(itemModel -> {
        Item item = itemModel.getItem();
        if (item == null) return false;
        if (!item.getAuctionType().equals(selectedAuctionType)) {
          return false;
        }
        if (minPrice != -1 && itemModel.currentPriceProperty().get() < minPrice) {
          return false;
        }
        if (maxPrice != -1 && itemModel.currentPriceProperty().get() > maxPrice) {
          return false;
        }
        if (targetCategory != null && !targetCategory.isBlank() && !targetCategory.equalsIgnoreCase("All")) {
          if (targetCategory.equals("ALL")) {
            return true;
          }
          if (!targetCategory.equalsIgnoreCase(item.getCategory())) {
            return false;
          }
        }
        return true;
      });
      refreshTrendingBid();
    });
  }

  private void registerLotsRow(String rowName, String category, ListPane<ItemModel> lots) {
    FilteredList<ItemModel> row = new FilteredList<>(filteredList,
      clientItem -> clientItem.getItem() != null && category.equalsIgnoreCase(clientItem.getItem().getCategory()));
    lots.setTitle(rowName);
    lots.setItems(row, ItemCard::new, onItemCardClicked);
  }

  @Override
  public void refreshData() {
    discoveryService.refreshOngoingLots();
  }

  private void refreshLeaderBoard() {
    discoveryService.fetchLeaderboardData(userModels -> {
      FXThread.run(() -> leaderboardContainer.setItems(userModels));
    });
  }

  private void refreshTrendingBid() {
    if (toggleGroup.getSelectedToggle() == null) return;
    AuctionType currentType = (AuctionType) toggleGroup.getSelectedToggle().getUserData();
    discoveryService.getTrendingLots()
      .thenAccept(trendingLots -> {
        List<ItemModel> itemModels = new ArrayList<>();
        for (Item item : trendingLots) {
          if (item != null && currentType.equals(item.getAuctionType())) {
            itemModels.add(new ItemModel(item));
          }
        }
        FXThread.run(() -> trendingBind.setItems(itemModels));
      }).exceptionally(throwable -> {
        throwable.printStackTrace();
        return null;
      });
  }

  @Override
  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
  }
}