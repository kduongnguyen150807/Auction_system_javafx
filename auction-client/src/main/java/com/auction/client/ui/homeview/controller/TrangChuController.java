package com.auction.client.ui.homeview.controller;

import com.auction.client.app.AutoInject;
import com.auction.client.service.auction.AuctionDetailService;
import com.auction.client.service.auction.AuctionDiscoveryService;
import com.auction.client.store.lotsinformation.OngoingLots;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.homeview.HomeViewType;
import com.auction.client.ui.homeview.homeviewcomponent.ItemCard;
import com.auction.client.ui.homeview.homeviewcomponent.ListPane;
import com.auction.client.ui.homeview.homeviewcomponent.RedOrBlueToolbar;
import com.auction.client.ui.homeview.homeviewcomponent.TrendingBind;
import com.auction.shared.AuctionType;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;

import java.util.function.Consumer;

public class TrangChuController implements CanRefresh, CanSwitchNode<HomeViewType> {
  private Consumer<HomeViewType> switchNode;

  FilteredList<ItemModel> filteredList = new FilteredList<>(OngoingLots.AUCTION_STORE.getOngoingClientItemList());

  @FXML private RedOrBlueToolbar<AuctionType> auctionTypeToggle;
  private final ToggleGroup toggleGroup = new ToggleGroup();

  @FXML private TrendingBind trendingBind;
  @FXML private ListPane<ItemModel> artLots;
  @FXML private ListPane<ItemModel> electronicsLots;
  @FXML private ListPane<ItemModel> vehiclesLots;

  private final AuctionDiscoveryService discoveryService;
  private final AuctionDetailService detailService;

  @AutoInject
  public TrangChuController(AuctionDiscoveryService discoveryService, AuctionDetailService detailService) {
    this.discoveryService = discoveryService;
    this.detailService = detailService;
  }

  @FXML
  public void initialize() {
    discoveryService.refreshItems();
    setToggleToolbar();
    initLotsRow();
  }

  private void setToggleToolbar() {
    auctionTypeToggle.setUpToggleGroup(toggleGroup);
    auctionTypeToggle.setData("Browse category", AuctionType.DUTCH, AuctionType.ENGLISH);
    toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null) {
        oldValue.setSelected(true);
        return;
      }
      filterAuctionType();
    });
    filterAuctionType();
  }

  @FXML
  private void categoryCarouselPrev() {}

  @FXML
  private void categoryCarouselNext() {}

  private void initLotsRow() {
    registerLotsRow("ARTS LOTS", "ART", artLots);
    registerLotsRow("VEHICLES LOTS", "VEHICLE", vehiclesLots);
    registerLotsRow("ELECTRONICS LOTS", "ELECTRONICS", electronicsLots);
  }

  private void filterAuctionType() {
    AuctionType selectedAuctionType = (AuctionType) toggleGroup.getSelectedToggle().getUserData();
    filteredList.setPredicate(clientItem -> clientItem.getItem().getAuctionType().equals(selectedAuctionType));
  }

  private void registerLotsRow(String rowName, String category, ListPane<ItemModel> lots) {
    FilteredList<ItemModel> row =
      new FilteredList<>(filteredList,
        clientItem -> clientItem.getItem().getCategory().equalsIgnoreCase(category));
    lots.setTitle(rowName);
    lots.setItems(
      row,
      ItemCard::new,
      clientItem -> {
        detailService.setSelectedItem(clientItem);
        switchNode.accept(HomeViewType.ITEM_INFORMATION);
      }
    );
  }

  @Override
  public void refreshData() {
    discoveryService.refreshItems();
  }

  @Override
  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
  }
}
