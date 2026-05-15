package com.auction.client.ui.homeview.homeviewcomponent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class SearchBar extends HBox {
  private static final String BASE_FXML_PATH = "/fxml/homeview/SearchBar.fxml";

  public SearchBar() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    loader.setController(this);
    loader.setRoot(this);

    try {
      loader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @FXML
  private void toggleNotifications() {

  }

  @FXML
  private void onSearchModeChanged() {

  }

  @FXML
  private void applyFilter() {

  }
}
