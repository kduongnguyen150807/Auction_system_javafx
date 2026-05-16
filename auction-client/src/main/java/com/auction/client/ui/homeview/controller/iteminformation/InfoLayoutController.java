package com.auction.client.ui.homeview.controller.iteminformation;

import com.auction.client.store.ClientItem;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.util.ImageViewUtils;
import com.auction.client.util.StringFormat;
import com.auction.client.util.TimeFormat;
import com.auction.shared.Item;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class InfoLayoutController {
  private ClientItem selectedItem;

  @FXML private Label itemName;
  @FXML private ImageView sellerAvatar;
  @FXML private Label sellerName;
  @FXML private Label itemDescription;
  @FXML private Label currentHighestBidValue;
  @FXML private Label maxPriceValue;
  @FXML private Label endsInValue;

  public void setSelectedItem(ClientItem clientItem) {
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

    if (url.isBlank() || url == null || url.isEmpty()) {
      sellerAvatar.setImage(null);
    }

    ImageViewUtils.setImageToImageView(sellerAvatar, url);
  }
}
