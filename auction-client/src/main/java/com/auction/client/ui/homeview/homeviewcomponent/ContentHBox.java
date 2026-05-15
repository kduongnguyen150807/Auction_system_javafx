package com.auction.client.ui.homeview.homeviewcomponent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;

import java.io.IOException;

public class ContentHBox extends HBox {
  private static final String BASE_FXML_PATH = "/fxml/component/ContentHBox.fxml";

  @FXML private Label nameHBox;
  @FXML private SVGPath svgHBox;

  public ContentHBox(String text, String SVGContent) {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setController(this);
    fxmlLoader.setRoot(this);

    try {
      fxmlLoader.load();
      nameHBox.setText(text);
      svgHBox.setContent(SVGContent);
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  public void activate() {
    getStyleClass().add("active");
  }

  public void deactivate() {
    getStyleClass().remove("active");
  }

}
