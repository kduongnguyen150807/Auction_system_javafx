package com.auction.client.ui.homeview.controller.iteminformation;

import com.auction.client.ui.base.CanRefresh;
import com.auction.client.util.StringFormat;
import com.auction.client.util.TimeFormat;
import com.auction.shared.Item;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class InfoLayoutController implements CanRefresh {
  private Item selectedItem;

  @FXML private Label itemName;
  @FXML private ImageView sellerAvatar;
  @FXML private Label sellerName;
  @FXML private Label itemDescription;
  @FXML private Label currentHighestBidValue;
  @FXML private Label maxPriceValue;
  @FXML private Label endsInValue;

  public void setSelectedItem(Item item) {
    selectedItem = item;
    refreshData();
  }

  private void setData() {
    if (selectedItem == null) {
      return;
    }

    itemName.setText(selectedItem.getName());
    itemDescription.setText(selectedItem.getDescription());
    sellerName.setText(selectedItem.getSellerUsername());
    itemDescription.setText(selectedItem.getDescription());
    currentHighestBidValue.setText(StringFormat.formatMoney(selectedItem.getCurrentPrice()));
    maxPriceValue.setText(StringFormat.formatMoney(selectedItem.getMaxPrice()));
    endsInValue.setText(TimeFormat.getRemainingTime(selectedItem.getEndTime()));
  }

  @Override
  public void refreshData() {
    setData();
  }
}
