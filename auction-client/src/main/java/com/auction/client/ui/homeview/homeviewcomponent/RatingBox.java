package com.auction.client.ui.homeview.homeviewcomponent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class RatingBox extends VBox {
  private static final String BASE_FXML_PATH = "/fxml/component/RattingBox.fxml";

  public RatingBox() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @FXML
  private void showRatingForm() {

  }

  @FXML
  private void handleRatingFilter() {

  }
}
