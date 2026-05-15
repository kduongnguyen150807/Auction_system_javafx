package com.auction.client.ui.homeview.controller;

import com.auction.client.service.AuctionService;
import com.auction.client.store.AuctionStore;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.homeview.HomeViewType;
import com.auction.client.ui.homeview.homeviewcomponent.ListPane;
import com.auction.client.ui.homeview.homeviewcomponent.RedOrBlueToolbar;
import com.auction.client.ui.homeview.homeviewcomponent.TrendingBind;
import com.auction.shared.ItemStatus;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;

import java.util.function.Consumer;

public class TrangChuController implements CanRefresh, CanSwitchNode<HomeViewType> {
  private static final String ENGLISH_AUCTION_TYPE = "English Auction";
  private static final String DUTCH_AUCTION_TYPE = "Dutch Auction";

  private Consumer<HomeViewType> switchNode;

  @FXML private RedOrBlueToolbar auctionTypeToggle;
  private final ToggleGroup toggleGroup = new ToggleGroup();

  @FXML private TrendingBind trendingBind;
  @FXML private ListPane artLots;

  @FXML
  public void initialize() {
    AuctionService.refreshItems();
    setToggleToolbar();
    initLotsRow();
  }

  private void setToggleToolbar() {
    auctionTypeToggle.setUpToggleGroup(toggleGroup);
    auctionTypeToggle.setData("Browse category", ENGLISH_AUCTION_TYPE, DUTCH_AUCTION_TYPE);
  }

  @FXML
  private void categoryCarouselPrev() {}

  @FXML
  private void categoryCarouselNext() {}

  private void initLotsRow() {
    artLots.setItems(AuctionStore.AUCTION_STORE.filterStatus(ItemStatus.OPEN));
  }

  @Override
  public void refreshData() {
  }

  @Override
  public void setSwitchNode(Consumer<HomeViewType> switchNode) {
    this.switchNode = switchNode;
    artLots.setSwitchNode(switchNode);
  }
}
