package com.auction.client.ui.homeview.controller;

import com.auction.client.service.AuctionService;
import com.auction.client.store.AuctionStore;
import com.auction.client.store.lotsinformation.ClientItem;
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

  FilteredList<ClientItem> filteredList = new FilteredList<>(AuctionStore.AUCTION_STORE.getOngoingClientItemList());

  @FXML private RedOrBlueToolbar<AuctionType> auctionTypeToggle;
  private final ToggleGroup toggleGroup = new ToggleGroup();

  @FXML private TrendingBind trendingBind;
  @FXML private ListPane<ClientItem> artLots;
  @FXML private ListPane<ClientItem> electronicsLots;
  @FXML private ListPane<ClientItem> vehiclesLots;

  @FXML
  public void initialize() {
    AuctionService.refreshItems();
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

  private void registerLotsRow(String rowName, String category, ListPane<ClientItem> lots) {
    FilteredList<ClientItem> row =
      new FilteredList<>(filteredList,
        clientItem -> clientItem.getItem().getCategory().equalsIgnoreCase(category));
    lots.setTitle(rowName);
    lots.setItems(
      row,
      ItemCard::new,
      clientItem -> {
        AuctionService.setSelectedItem(clientItem);
        switchNode.accept(HomeViewType.ITEM_INFORMATION);
      }
    );
  }

  @Override
  public void refreshData() {
    AuctionService.refreshItems();
  }

  @Override
  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
  }
}
