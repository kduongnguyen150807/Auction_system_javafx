package com.auction.client.ui.homeview.homeviewcomponent;

import com.auction.client.ui.base.CanRefresh;
import com.auction.shared.Item;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class TrendingBind extends VBox implements CanRefresh {
  private static final String BASE_FXML_PATH = "/fxml/component/TrendingBind.fxml";

  public TrendingBind() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void refreshData() {

  }
}
