package com.auction.client.ui.homeview.controller.iteminformation;

import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.util.ImageViewUtils;
import com.auction.client.util.StringFormat;
import com.auction.client.util.TimeFormat;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class InfoLayoutController {
  private ItemModel selectedItem;

  @FXML private Label itemName;
  @FXML private ImageView sellerAvatar;
  @FXML private Label sellerName;
  @FXML private Label itemDescription;
  @FXML private Label currentHighestBidValue;
  @FXML private Label maxPriceValue;
  @FXML private Label endsInValue;

  public void setSelectedItem(ItemModel clientItem) {
    unbind();
    this.selectedItem = clientItem;
    loadSellerAvatar(clientItem.getItem().getSellerAvatarUrl());
    bind();
  }

  private void unbind() {
    itemName.textProperty().unbind();
    itemDescription.textProperty().unbind();
    currentHighestBidValue.textProperty().unbind();
    endsInValue.textProperty().unbind();
  }

  private void bind() {
    if (selectedItem == null) {
      return;
    }

    itemName.textProperty().bind(selectedItem.nameProperty());
    itemDescription.textProperty().bind(selectedItem.descriptionProperty());
    currentHighestBidValue.textProperty().bind(selectedItem.currentPriceProperty().asString("$ %.2f"));
    maxPriceValue.setText(StringFormat.formatMoney(selectedItem.getItem().getMaxPrice()));
    endsInValue.textProperty().bind(Bindings.createStringBinding(
      () -> TimeFormat.getRemainingTime(selectedItem.getItem().getEndTime()),
      selectedItem.endTimeProperty()
    ));

    sellerName.setText(selectedItem.getItem().getSellerUsername());
  }

  private void loadSellerAvatar(String url) {
    if (sellerAvatar == null) {
      return;
    }

    if (url == null || url.isBlank()) {
      sellerAvatar.setImage(null);
      return;
    }

    ImageViewUtils.setImageToImageView(sellerAvatar, url);
  }
}
