package com.auction.client.ui.SearchBar;

import com.auction.client.ui.Main.KhungController;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class ThanhTimKiemController {
  @FXML private TextField searchField;
  @FXML private ComboBox<String> categoryFilter;

  @FXML
  void initialize() {
    categoryFilter.getItems().addAll("All", "Electronics", "Art", "Vehicle");
    categoryFilter.setValue("All");
    searchField.textProperty().addListener((obs, oldV, newV) -> publishFilters());
    categoryFilter.valueProperty().addListener((obs, oldV, newV) -> publishFilters());
    publishFilters();
  }

  private void publishFilters() {
    String keyword = searchField == null ? "" : searchField.getText();
    String category = categoryFilter == null ? "All" : categoryFilter.getValue();
    KhungController.applySearchFilter(keyword, category);
  }
}
