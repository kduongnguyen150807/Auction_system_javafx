package com.auction.client.ui.homeview.homeviewcomponent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class RedOrBlueToolbar<T> extends HBox {
  private static final String BASE_FXML_PATH = "/fxml/component/RedOrBlueToolbar.fxml";

  @FXML private Label titleLabel;
  @FXML private ToggleButton red;
  @FXML private ToggleButton blue;

  public RedOrBlueToolbar() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setUpToggleGroup(ToggleGroup group) {
    red.setToggleGroup(group);
    blue.setToggleGroup(group);

    /* default red selected */
    red.setSelected(true);
  }

  public void setData(String title, T red, T blue) {
    titleLabel.setText(title);
    this.red.setText(red.toString());
    this.blue.setText(blue.toString());

    this.red.setUserData(red);
    this.blue.setUserData(blue);
  }
}
