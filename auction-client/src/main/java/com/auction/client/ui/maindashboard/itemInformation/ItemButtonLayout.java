package com.auction.client.ui.maindashboard.itemInformation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ItemButtonLayout extends VBox {
  private static final String BASE_FXML_PATH = "/fxml/MainDashBoard/itemInformation/ItemButtonLayout.fxml";

  public ItemButtonLayout() {
    initBaseLayout();
  }

  private void initBaseLayout() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
      System.out.println("load ItemButtonLayout");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void handleBid(ActionEvent actionEvent) {
  }

  @FXML
  public void handleAutoBid(ActionEvent actionEvent) {
  }

  @FXML
  public void handleRate(ActionEvent actionEvent) {
  }
}
