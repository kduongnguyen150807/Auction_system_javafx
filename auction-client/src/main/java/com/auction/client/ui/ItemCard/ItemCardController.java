package com.auction.client.ui.ItemCard;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.Main.KhungController;
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

  public void setData(String name, double price, String desc, String time, String imageUrl) {
    ItemName.setText(name);
    ItemDescription.setText(desc);
    Price.setText(String.format("%,.2f$", price));
    TimeRemain.setText(time);
    if (imageUrl != null && !imageUrl.isBlank()) {
      String u = imageUrl.contains(".webp") ? imageUrl.replace(".webp", ".jpg") : imageUrl;
      ImageHolder.setImage(new Image(u, true));
    }
  }

  public void HandleItemClicked() {
    try {
      NodeContentLoader<ScrollPane> infoLoader = new NodeContentLoader<>();
      infoLoader.load("/fxml/iteminformation/ItemInformation.fxml");
      NodeManager.switchNodewithNode(
          infoLoader.getCurrentNode(),
          KhungController.getCurrentNode(),
          KhungController.getKhungChua());
    } catch (Exception ignored) {
    }
  }
}
