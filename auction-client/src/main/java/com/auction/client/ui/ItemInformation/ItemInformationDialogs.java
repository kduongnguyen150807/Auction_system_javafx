package com.auction.client.ui.ItemInformation;

import javafx.scene.control.Alert;

final class ItemInformationDialogs {

  private ItemInformationDialogs() {}

  static void show(Alert.AlertType type, String title, String content) {
    try {
      Alert a = new Alert(type);
      a.setTitle(title);
      a.setHeaderText(null);
      a.setContentText(content);
      a.showAndWait();
    } catch (Exception ignored) {
      // headless / toolkit edge cases
    }
  }
}
