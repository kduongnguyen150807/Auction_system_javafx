package com.auction.client.ui.homeview.controller.iteminformation;

import com.auction.client.app.AutoInject;
import com.auction.client.service.auction.BiddingService;
import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.store.selectediteminformation.SelectedItem;
import com.auction.client.store.selectediteminformation.SelectedItemBidHistory;
import com.auction.client.ui.homeview.homeviewcomponent.BidChart;
import com.auction.client.util.ImageViewUtils;
import com.auction.client.util.TimeFormat;
import com.auction.shared.Item;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

public class ItemInformationController {
  @FXML private InfoLayoutController infoLayoutController;
  @FXML private ButtonLayoutController buttonLayoutController;
  @FXML private BidChart bidChart;


  @FXML private ImageView itemImageHolder;

  private final BiddingService biddingService;

  @AutoInject
  public ItemInformationController(BiddingService biddingService) {
    this.biddingService = biddingService;
  }

  @FXML
  public void initialize() {
    SelectedItem.SELECTED_ITEM.selectedItemProperty()
      .addListener((observable, oldValue, newValue) -> {
        applyItem(newValue);
      });
    applyItem(SelectedItem.SELECTED_ITEM.getSelectedItem());

    bidChart.setSelectedBidHistory(
      SelectedItemBidHistory.SELECTED_ITEM_BID_HISTORY.selectedItemBidHistoryProperty(),
      TimeFormat.DMY_HM,
      20
    );
  }

  private void applyItem(ItemModel item) {
    if (item == null) {
      System.out.println("null");
      return;
    }
    infoLayoutController.setSelectedItem(item);
    buttonLayoutController.setSelectedItem(item);
    loadImage(item.getItem());
  }

  private void loadImage(Item item) {
    if  (itemImageHolder == null) {
      return;
    }
    if (item.getImageUrl() == null || item.getImageUrl().isBlank()) {
      itemImageHolder.setImage(null);
      return;
    }
    ImageViewUtils.setImageToImageView(itemImageHolder, item.getImageUrl());
  }
}
