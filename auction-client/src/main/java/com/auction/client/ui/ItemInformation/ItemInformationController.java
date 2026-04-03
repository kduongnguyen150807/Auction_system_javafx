package com.auction.client.ui.ItemInformation;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.Main.KhungController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ItemInformationController {
  @FXML private ImageView ItemImageHolder;
  @FXML private Label ItemName;
  @FXML private Label ItemDescription;
  @FXML private Label CurrentHighestBidValue;
  @FXML private Label EndsInValue;

  private int itemId = -1;
  private String itemName = "";

  public void setData(
      int itemId,
      String name,
      double currentHighestBid,
      String description,
      String endsIn,
      String imageUrl) {
    this.itemId = itemId;
    this.itemName = name == null ? "" : name;

    if (ItemName != null) ItemName.setText(this.itemName);
    if (ItemDescription != null) ItemDescription.setText(description == null ? "" : description);
    if (CurrentHighestBidValue != null)
      CurrentHighestBidValue.setText(String.format("%,.2f$", currentHighestBid));
    if (EndsInValue != null) EndsInValue.setText(endsIn == null ? "" : endsIn);

    if (ItemImageHolder != null && imageUrl != null && !imageUrl.isBlank()) {
      String u =
          imageUrl.contains(".webp") ? imageUrl.replace(".webp", ".jpg") : imageUrl;
      ItemImageHolder.setImage(new Image(u, true));
    }
  }

  @FXML
  private void ShowBiddingForm() {
    try {
      NodeContentLoader<VBox> formLoader = new NodeContentLoader<>();
      formLoader.load("/fxml/biddingform/BiddingForm.fxml");

      Object controller = formLoader.getController();
      if (controller instanceof com.auction.client.ui.BiddingForm.BiddingFormController) {
        com.auction.client.ui.BiddingForm.BiddingFormController bidding =
            (com.auction.client.ui.BiddingForm.BiddingFormController) controller;
        if (itemId > 0) bidding.setData(itemId, itemName);
      }

      NodeManager.addNodeToPane(formLoader, KhungController.getKhungChua());
    } catch (Exception ignored) {
    }
  }
}
