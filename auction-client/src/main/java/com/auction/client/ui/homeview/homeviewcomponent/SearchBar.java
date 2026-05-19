package com.auction.client.ui.homeview.homeviewcomponent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.function.Consumer;

public class SearchBar<T> extends HBox {
  private static final String BASE_FXML_PATH = "/fxml/homeview/SearchBar.fxml";

  @FXML private TextField searchField;
  @FXML private Button applyButton;

  private Consumer<String> onSearch;

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
  private void handleApply() {
    if (onSearch == null) {
      return;
    }

    String query = searchField.getText();

    if (query.isEmpty()) {
      return;
    }

    onSearch.accept(query.trim());
  }

  public void setOnSearch(Consumer<String> onSearch) {
    this.onSearch = onSearch;
  }
}
