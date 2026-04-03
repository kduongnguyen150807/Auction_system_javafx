package com.auction.client.ui.ItemCard;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.Main.KhungController;
import com.auction.client.ui.ItemInformation.ItemInformationController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ItemCardController {
  @FXML private VBox itemRoot;
  @FXML private Label ItemName;
  @FXML private Label ItemDescription;
  @FXML private Label Price;
  @FXML private Label TimeRemain;
  @FXML private ImageView ImageHolder;

  private int itemId = -1;
  private String itemName = "";
  private String itemDesc = "";
  private String itemTimeRemain = "";
  private double currentPrice = 0.0;
  private String itemImageUrl = "";

  public void setData(int itemId, String name, double price, String desc, String time, String imageUrl) {
    this.itemId = itemId;
    this.itemName = name == null ? "" : name;
    this.itemDesc = desc == null ? "" : desc;
    this.itemTimeRemain = time == null ? "" : time;
    this.currentPrice = price;
    this.itemImageUrl = imageUrl == null ? "" : imageUrl;

    ItemName.setText(name);
    ItemDescription.setText(this.itemDesc);
    Price.setText(String.format("%,.2f$", price));
    TimeRemain.setText(time);
    if (!this.itemImageUrl.isBlank()) {
      String u = this.itemImageUrl.contains(".webp") ? this.itemImageUrl.replace(".webp", ".jpg") : this.itemImageUrl;
      ImageHolder.setImage(new Image(u, true));
    }
  }

  public void HandleItemClicked() {
    try {
      NodeContentLoader<ScrollPane> infoLoader = new NodeContentLoader<>();
      infoLoader.load("/fxml/iteminformation/ItemInformation.fxml");

      ItemInformationController infoController = infoLoader.getController();
      if (infoController != null && itemId > 0) {
        infoController.setData(itemId, itemName, currentPrice, itemDesc, itemTimeRemain, itemImageUrl);
      }

      NodeManager.switchNodewithNode(
          infoLoader.getCurrentNode(),
          KhungController.getCurrentNode(),
          KhungController.getKhungChua());
      KhungController.setMainContentNode(infoLoader.getCurrentNode());
    } catch (Exception ignored) {
    }
  }
}
