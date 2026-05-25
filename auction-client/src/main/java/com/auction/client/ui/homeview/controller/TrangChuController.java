package com.auction.client.ui.homeview.controller;

import com.auction.client.app.AutoInject;
import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.service.auction.AuctionDiscoveryService;
import com.auction.client.service.user.ClientService;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.lotsinformation.ItemModelRegistry;
import com.auction.client.store.lotsinformation.OpenLots;
import com.auction.client.store.userinformation.UserModel;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.component.FilterBox;
import com.auction.client.ui.component.itemcard.ItemCardConfig;
import com.auction.client.ui.component.userbar.UserBar;
import com.auction.client.ui.component.userbar.UserBarMode;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TrangChuController implements CanRefresh, CanSwitchNode<HomeViewType> {
  private Consumer<HomeViewType> switchNode;

  private final FilteredList<ItemModel> filterBoxManagedList = new FilteredList<>(OpenLots.AUCTION_STORE.getOngoingClientItemList());

  private final FilteredList<ItemModel> auctionTypeFilteredList = new FilteredList<>(filterBoxManagedList);

  @FXML private RedOrBlueToolbar<AuctionType> auctionTypeToggle;
  private final ToggleGroup toggleGroup = new ToggleGroup();

  @FXML private VBoxModel<ItemModel> trendingBind;
  @FXML private VBoxModel<UserModel> leaderboardContainer;
  @FXML private ListPane<ItemModel> artLots;
  @FXML private ListPane<ItemModel> electronicsLots;
  @FXML private ListPane<ItemModel> vehiclesLots;
  @FXML private FilterBox filterBox;

  private final AuctionDiscoveryService discoveryService;
  private AuctionDetailService detailService;
  private ClientService clientService;

  private Timeline countdownTimeline;

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
  public TrangChuController(AuctionDiscoveryService discoveryService, AuctionDetailService detailService, ClientService clientService) {
    this.discoveryService = discoveryService;
    this.detailService = detailService;
    this.clientService = clientService;
  }

  @FXML
  public void initialize() {
    filterBox.registerListsToFilter(List.of(filterBoxManagedList));
    filterBox.setOnApply(this::applyAuctionTypeFilter);

    discoveryService.refreshOngoingLots();
    setToggleToolbar();
    initLotsRow();

    refreshTrendingBid();
    refreshLeaderBoard();
    countdownTimeline = TimelineUtils.setTimeline(countdownTimeline, 30, List.of(this::refreshLeaderBoard, this::refreshTrendingBid));
  }

  private void setToggleToolbar() {
    auctionTypeToggle.setUpToggleGroup(toggleGroup);
    auctionTypeToggle.setData("Browse category", AuctionType.ENGLISH, AuctionType.DUTCH);

    toggleGroup.selectedToggleProperty().addListener((_, oldValue, newValue) -> {
      if (newValue == null) {
        if (oldValue != null) oldValue.setSelected(true);
        return;
      }
      applyAuctionTypeFilter();
    });
  }

  private void initLotsRow() {
    registerLotsRow("ARTS LOTS", "ART", artLots);
    registerLotsRow("VEHICLES LOTS", "VEHICLE", vehiclesLots);
    registerLotsRow("ELECTRONICS LOTS", "ELECTRONICS", electronicsLots);
  }

  private void applyAuctionTypeFilter() {
    if (toggleGroup.getSelectedToggle() == null) return;
    AuctionType selectedAuctionType = (AuctionType) toggleGroup.getSelectedToggle().getUserData();

    FXThread.run(() -> {
      auctionTypeFilteredList.setPredicate(itemModel -> {
        Item item = itemModel.getItem();
        return item != null && selectedAuctionType.equals(item.getAuctionType());
      });

      refreshTrendingBid();
    });
  }

  private void registerLotsRow(String rowName, String category, ListPane<ItemModel> lots) {
    FilteredList<ItemModel> categoryRowList = new FilteredList<>(auctionTypeFilteredList,
      clientItem -> clientItem.getItem() != null && category.equalsIgnoreCase(clientItem.getItem().getCategory()));

    lots.setTitle(rowName);
    lots.bind(categoryRowList, itemCardConfig.cardFactory());
  }

  @Override
  public void refreshData() {
    discoveryService.refreshOngoingLots();
    refreshLeaderBoard();
    refreshTrendingBid();
  }

  private void refreshLeaderBoard() {
    leaderboardContainer.getChildren().clear();
    discoveryService.fetchLeaderboardData(userModels ->
      leaderboardContainer.setAll(
        userModels,
        param -> {
          UserBar userBar = new UserBar(param);
          userBar.setDisplayMode(UserBarMode.NONE);
          userBar.setOnUserBarClicked(userModel -> {
            detailService.setSelectedUser(userModel);
            switchNode.accept(HomeViewType.USER_INFORMATION);
          });
          return userBar;
        }
      ));
  }

  private void refreshTrendingBid() {
    if (toggleGroup.getSelectedToggle() == null) return;
    AuctionType currentType = (AuctionType) toggleGroup.getSelectedToggle().getUserData();

    discoveryService.getTrendingLots()
      .thenAccept(trendingLots -> {
        if (trendingLots == null) return;

        List<ItemModel> itemModels = new ArrayList<>();
        for (Item item : trendingLots) {
          if (item != null && currentType.equals(item.getAuctionType())) {
            itemModels.add(ItemModelRegistry.getOrCreate(item));
          }
        }

        FXThread.run(() -> trendingBind.setAll(itemModels, itemCardConfig.cardFactory()));
      });
  }

  @Override
  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
  }
}