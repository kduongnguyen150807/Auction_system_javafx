package com.auction.client.ui.maindashboard.itemInformation;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ItemInfoLayout extends VBox {
  private static final String BASE_FXML_PATH = "/fxml/MainDashBoard/itemInformation/ItemInfoLayout.fxml";

  public ItemInfoLayout() {
    initBaseLayout();
  }

  private void initBaseLayout() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
      System.out.println("load ItemInfoLayout");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
