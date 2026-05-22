package com.auction.client.ui.homeview.homeviewcomponent;

import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.util.ImageViewUtils;
import com.auction.client.util.TimeFormat;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;

public class ItemCard extends VBox {
  private static final String BASE_FXML_PATH = "/fxml/component/ItemCard.fxml";

  private final ItemModel itemModel;

  @FXML private ImageView imageHolder;
  @FXML private Label heartIcon;
  @FXML private Label itemName;
  @FXML private Label itemDescription;
  @FXML private Label price;
  @FXML private Label timeRemain;

  public ItemCard(ItemModel clientItem) {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setController(this);
    fxmlLoader.setRoot(this);


    try {
      fxmlLoader.load();
      this.itemModel = clientItem;
      setData(clientItem);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void setData(ItemModel clientItem) {
    itemName.textProperty().bind(clientItem.nameProperty());
    itemDescription.textProperty().bind(clientItem.descriptionProperty());
    price.textProperty().bind(clientItem.currentPriceProperty().asString("$ %.2f"));

    setUpTimeRemain(clientItem);
    loadImageIfPresent(clientItem.getItem().getImageUrl());
  }

  private void setUpTimeRemain(ItemModel clientItem) {
    LocalDateTime endTime = itemModel.endTimeProperty().get();
    Item item =  clientItem.getItem();
    if (!item.getStatus().equals(ItemStatus.OPEN)) {
      timeRemain.setText(item.getStatus().name().toUpperCase());
    } else {
      timeRemain.textProperty().bind(
        Bindings.createStringBinding(() -> {
          String remainingTime = TimeFormat.getDHM(clientItem.endTimeProperty().get());
          return String.format(remainingTime);
        }, clientItem.endTimeProperty())
      );
    }
  }

  private void loadImageIfPresent(String imageUrl) {
    if (imageHolder == null || imageUrl == null || imageUrl.isBlank()) {
      return;
    }

    ImageViewUtils.setImageToImageView(imageHolder, imageUrl);
  }

  public void dispose() {
    if (itemName != null) itemName.textProperty().unbind();
    if (itemDescription != null) itemDescription.textProperty().unbind();
    if (price != null) price.textProperty().unbind();

    if (timeRemain != null) {
      timeRemain.textProperty().unbind();
    }

    if (imageHolder != null) {
      imageHolder.setImage(null);
    }
  }

  public ItemModel getItemModel() {
    return itemModel;
  }
}
