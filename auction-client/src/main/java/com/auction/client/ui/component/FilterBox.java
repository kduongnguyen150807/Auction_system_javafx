package com.auction.client.ui.component;

import com.auction.client.store.lotsinformation.ItemModel;
import com.auction.shared.Item;
import com.auction.client.util.FXThread;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FilterBox extends HBox {
  private static final String BASE_FXML_PATH = "/fxml/component/FilterBox.fxml";

  @FXML private ComboBox<String> categoryFilter;
  @FXML private IntegerField minPriceField;
  @FXML private IntegerField maxPriceField;

  private Runnable onApplyCallback;

  private final List<FilteredList<ItemModel>> registeredLists = new ArrayList<>();

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
    categoryFilter.setValue("ALL");

    categoryFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
      executeInternalFilter();
    });
  }

  public void registerListsToFilter(List<FilteredList<ItemModel>> lists) {
    if (lists != null) {
      this.registeredLists.addAll(lists);
      executeInternalFilter();
    }
  }

  public void setOnApply(Runnable onApplyCallback) {
    this.onApplyCallback = onApplyCallback;
  }

  @FXML
  private void applyFilter() {
    executeInternalFilter();
  }

  public void executeInternalFilter() {
    int minPrice = getMinPrice();
    int maxPrice = getMaxPrice();
    String targetCategory = getCategory();

    Predicate<ItemModel> filterPredicate = itemModel -> {
      Item item = itemModel.getItem();
      if (item == null) return false;

      if (minPrice != -1 && itemModel.currentPriceProperty().get() < minPrice) return false;

      if (maxPrice != -1 && itemModel.currentPriceProperty().get() > maxPrice) return false;

      if (targetCategory != null && !targetCategory.isBlank() && !targetCategory.equalsIgnoreCase("ALL")) {
        return item.getCategory().equalsIgnoreCase(targetCategory);
      }

      return true;
    };

    FXThread.run(() -> {
      for (FilteredList<ItemModel> filteredList : registeredLists) {
        filteredList.setPredicate(filterPredicate);
      }

      if (onApplyCallback != null) {
        onApplyCallback.run();
      }
    });
  }

  public int getMinPrice() { return minPriceField.getValue(); }
  public int getMaxPrice() { return maxPriceField.getValue(); }
  public String getCategory() { return categoryFilter.getValue(); }
}