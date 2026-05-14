package com.auction.client.ui.maindashboard.itemInformation;

import com.auction.client.ClientSession;
import com.auction.client.service.AuctionService;
import com.auction.client.ui.utils.StringFormat;
import com.auction.client.ui.utils.TimeUI;
import com.auction.shared.dto.BidForm;
import com.auction.shared.item.Item;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ItemInfoLayoutController {
  @FXML private Label itemName;
  @FXML private Label sellerName;
  @FXML private Label description;
  @FXML private Label currentBid;
  @FXML private Label buyNowPrice;
  @FXML private Label countdown;

  void setFocusedItem(ObjectProperty<Item> item) {
    itemName.textProperty().bind(item.map(Item::getName));
    sellerName.textProperty().bind(item.map(Item::getSellerUsername));
    description.textProperty().bind(item.map(Item::getDescription));
    currentBid.textProperty().bind(item.map(i -> {
      if (i == null) return "0$";
      return StringFormat.formatMoney(i.getCurrentPrice());
    }));
    buyNowPrice.textProperty().bind(item.map(i -> {
      if (i == null) return "0$";
      return StringFormat.formatMoney(i.getMaxPrice());
    }));
    countdown.textProperty().bind(item.map(i -> {
      return TimeUI.getRemainingTime(i.getEndTime());
    }));
  }
}
