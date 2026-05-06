package com.auction.client.ui.component;

import com.auction.shared.Lot;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ItemCard extends VBox {
  private static final String BASE_FXML_PATH = "/fxml/Component/ItemCard.fxml";
  private static final String BASE_STYLESHEET_PATH = "/css/Component/ItemCard.css";

  public ItemCard() {
    initBaseLayout();
  }

  private void initBaseLayout() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    loader.setRoot(this);
    loader.setController(this);

    this.getStylesheets().add(BASE_STYLESHEET_PATH);
    try {
      loader.load();
    } catch (IOException e) {
      throw new RuntimeException("Critical UI load failure", e);
    }
  }

  public void setData(Lot lot) {

  }
}
