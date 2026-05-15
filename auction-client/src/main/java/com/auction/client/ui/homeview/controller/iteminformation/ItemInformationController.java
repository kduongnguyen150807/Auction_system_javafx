package com.auction.client.ui.homeview.controller.iteminformation;

import com.auction.client.store.SelectedItem;
import com.auction.client.ui.base.CanRefresh;
import com.auction.shared.Item;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

public class ItemInformationController implements CanRefresh {
  @FXML private InfoLayoutController infoLayoutController;
  @FXML private ButtonLayoutController buttonLayoutController;

  @FXML private ImageView itemImageHolder;

  @FXML
  void initialize() {
    SelectedItem.SELECTED_ITEM.selectedItemProperty().addListener((observable, oldValue, item) -> {
      if (item != null) {
        applyItem(item);
      }
    });
  }

  @Override
  public void refreshData() {
    Item item = SelectedItem.SELECTED_ITEM.getSelectedItem();
    if (item != null) {
      applyItem(item);
    }
  }

  private void applyItem(Item item) {
    infoLayoutController.setSelectedItem(item);
  }
}
