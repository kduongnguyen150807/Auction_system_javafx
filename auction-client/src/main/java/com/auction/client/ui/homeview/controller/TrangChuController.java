package com.auction.client.ui.homeview.controller;

import com.auction.client.service.AuctionService;
import com.auction.client.store.AuctionStore;
import com.auction.client.store.ClientItem;
import com.auction.client.store.SelectedItem;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.homeview.HomeViewType;
import com.auction.client.ui.homeview.homeviewcomponent.ItemCard;
import com.auction.client.ui.homeview.homeviewcomponent.ListPane;
import com.auction.client.ui.homeview.homeviewcomponent.RedOrBlueToolbar;
import com.auction.client.ui.homeview.homeviewcomponent.TrendingBind;
import com.auction.shared.AuctionType;
import com.auction.shared.ItemStatus;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;

import java.util.function.Consumer;

public class TrangChuController implements CanRefresh, CanSwitchNode<HomeViewType> {
  private Consumer<HomeViewType> switchNode;

  FilteredList<ClientItem> filteredList = new FilteredList<>(AuctionStore.AUCTION_STORE.getClientItems());
  FilteredList<ClientItem> artRows =
    new FilteredList<>(filteredList,
      clientItem -> clientItem.getItem().getCategory().equalsIgnoreCase("ART"));
  FilteredList<ClientItem> electronicsRows =
    new FilteredList<>(filteredList,
      clientItem -> clientItem.getItem().getCategory().equalsIgnoreCase("ELECTRONICS"));
  FilteredList<ClientItem> vehiclesRows =
    new FilteredList<>(filteredList,
      clientItem -> clientItem.getItem().getCategory().equalsIgnoreCase("VEHICLE"));

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
    artLots.setTitle("ART LOTS");
    artLots.setItems(
      artRows,
      ItemCard::new,
      clientItem -> {
        AuctionService.setSelectedItem(clientItem);
        switchNode.accept(HomeViewType.ITEM_INFORMATION);
      });

    electronicsLots.setTitle("ELECTRONICS LOTS");
    electronicsLots.setItems(
      electronicsRows,
      ItemCard::new,
      clientItem -> {
        AuctionService.setSelectedItem(clientItem);
        switchNode.accept(HomeViewType.ITEM_INFORMATION);
      });

    vehiclesLots.setTitle("VEHICLES LOTS");
    vehiclesLots.setItems(
      vehiclesRows,
      ItemCard::new,
      clientItem -> {
        AuctionService.setSelectedItem(clientItem);
        switchNode.accept(HomeViewType.ITEM_INFORMATION);
      });
  }

  private void filterAuctionType() {
    AuctionType selectedAuctionType = (AuctionType) toggleGroup.getSelectedToggle().getUserData();
    filteredList.setPredicate(clientItem -> clientItem.getItem().getAuctionType().equals(selectedAuctionType));
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
