package com.auction.client.ui.component;

import com.auction.client.store.lotsinformation.ItemModel;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.List;

public class FilterBox extends HBox {
  private static final String BASE_FXML_PATH = "/fxml/component/FilterBox.fxml";

  @FXML private ComboBox<String> categoryFilter;
  @FXML private IntegerField minPriceField;
  @FXML private IntegerField maxPriceField;

  private Runnable onApplyCallback;

  public FilterBox() {
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
  private void initialize() {
    categoryFilter.getItems().addAll(List.of("ALL", "ART", "ELECTRONICS", "VEHICLE"));
    categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (onApplyCallback != null) {
        onApplyCallback.run();
      }
    });
  }

  public void setOnApply(Runnable onApplyCallback) {
    this.onApplyCallback = onApplyCallback;
  }

  @FXML
  private void applyFilter() {
    if (onApplyCallback != null) {
      onApplyCallback.run();
    }
  }

  public int getMinPrice() { return minPriceField.getValue(); }
  public int getMaxPrice() { return maxPriceField.getValue(); }
  public String getCategory() { return categoryFilter.getValue(); }
}
