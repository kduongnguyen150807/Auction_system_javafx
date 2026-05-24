package com.auction.client.ui.component.itemcard;

import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.client.ui.base.CanBind;
import com.auction.client.util.ImageViewUtils;
import com.auction.client.util.TimeFormat;
import com.auction.shared.ItemStatus;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.function.Consumer;

public class ItemCard extends VBox implements CanBind {
  private static final String BASE_FXML_PATH = "/fxml/component/ItemCard.fxml";

  private final ItemModel itemModel;
  private ObservableSet<Integer> idSet;

  private Consumer<ItemModel> onItemClicked;
  private Consumer<ItemModel> onHeartClicked;

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
      bind();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void bind() {
    if (itemModel == null || itemModel.getItem() == null) {
      return;
    }

    itemName.setText(itemModel.getItem().getName());
    itemDescription.setText(itemModel.getItem().getDescription());
    price.textProperty().bind(Bindings.format("$ %.2f", itemModel.currentPriceProperty()));

    if (itemModel.getItem().getStartTime().isAfter(LocalDateTime.now())) {
      timeRemain.setText("UPCOMING");
    } else if (itemModel.getStatus().equals(ItemStatus.OPEN)) {
      timeRemain.textProperty().bind(Bindings.createStringBinding(() -> {
        return TimeFormat.getDHM(itemModel.endTimeProperty().get());
      }, itemModel.endTimeProperty()));
    } else {
      timeRemain.setText(itemModel.getStatus().name());
    }

    heartIcon.setOnMouseClicked(event -> {
      event.consume();
      if (onHeartClicked != null) {
        onHeartClicked.accept(itemModel);
      }
    });

    String url = itemModel.getItem().getImageUrl();
    if (url == null || url.isEmpty()) {
      return;
    }

    ImageViewUtils.setImageToImageView(imageHolder, url);
  }


  @Override
  public void unbind() {
    itemName.textProperty().unbind();
    itemDescription.textProperty().unbind();
    price.textProperty().unbind();
    timeRemain.textProperty().unbind();
  }

  @Override
  public void dispose() {
    unbind();
    this.onItemClicked = null;
    this.onHeartClicked = null;
    this.idSet = null;
  }

  @FXML
  private void handleClick(MouseEvent e) {
    if (onItemClicked != null) {
      onItemClicked.accept(itemModel);
    }
  }

  public void setOnItemClicked(Consumer<ItemModel> onItemClicked) {
    this.onItemClicked = onItemClicked;
  }

  public void setOnHeartClicked(Consumer<ItemModel> onHeartClicked, ObservableSet<Integer> watchSet) {
    this.onHeartClicked = onHeartClicked;
    this.idSet = watchSet;

    idSet.addListener((SetChangeListener<? super Integer>)  change -> {
      if (change.wasAdded() && change.getElementAdded().equals(itemModel.getItem().getId())) {
        updateHeartUI(true);
      } else if (change.wasRemoved() && change.getElementRemoved().equals(itemModel.getItem().getId())) {
        updateHeartUI(false);
      }
    });

    boolean isWatched = watchSet.contains(itemModel.getId());
    updateHeartUI(isWatched);
  }

  public void updateHeartUI(boolean isWatching) {
    if (isWatching) {
      heartIcon.setText("❤");
      heartIcon.setStyle( "-fx-text-fill: #ff2a6d; -fx-font-size: 24px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0, 0, 2);");
    } else {
      heartIcon.setText("♡");
      heartIcon.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0, 0, 2);");
    }
  }

  public ItemModel getItemModel() {
    return itemModel;
  }
}
