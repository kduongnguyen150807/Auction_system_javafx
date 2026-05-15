package com.auction.client.ui.homeview.homeviewcomponent;

import com.auction.client.util.StringFormat;
import com.auction.client.util.TimeFormat;
import com.auction.shared.Item;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ItemCard extends VBox {
  private static final String BASE_FXML_PATH = "/fxml/component/ItemCard.fxml";

  private final Item item;

  @FXML private ImageView imageHolder;
  @FXML private Label heartIcon;
  @FXML private Label itemName;
  @FXML private Label itemDescription;
  @FXML private Label price;
  @FXML private Label timeRemain;

  public ItemCard(Item item) {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setController(this);
    fxmlLoader.setRoot(this);

    this.item = item;
    try {
      fxmlLoader.load();
      setData(item);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void setData(Item item) {
    itemName.setText(item.getName());
    itemDescription.setText(item.getDescription());
    price.setText(StringFormat.formatMoney(item.getCurrentPrice()));
    timeRemain.setText(TimeFormat.getRemainingTime(item.getEndTime()));
  }

  @FXML
  private void handleItemClicked() {

  }

  public Item getItem() {
    return item;
  }
}
