package com.auction.client.util;

import javafx.scene.control.Alert;

public class AlertUtil {
  public static void showAlert(Alert.AlertType type, String title, String content) {
    try {
      Alert res = new Alert(type);
      res.setTitle(title);
      res.setHeaderText(null);
      res.setContentText(content);
      res.showAndWait();
    } catch (Exception ignored) {
    }
  }

  public static void showErrorAlert(String title, String content) {
    try {
      Alert res = new Alert(Alert.AlertType.ERROR);
      res.setTitle(title);
      res.setHeaderText(null);
      res.setContentText(content);
      res.showAndWait();
    } catch (Exception ignored) {
    }
  }

  public static void showWarningAlert(String title, String content) {
    try {
      Alert res = new Alert(Alert.AlertType.WARNING);
      res.setTitle(title);
      res.setHeaderText(null);
      res.setContentText(content);
      res.showAndWait();
    } catch (Exception ignored) {
    }
  }

  public static void showInfoAlert(String title, String content) {
    try {
      Alert res = new Alert(Alert.AlertType.INFORMATION);
      res.setTitle(title);
      res.setHeaderText(null);
      res.setContentText(content);
      res.showAndWait();
    } catch (Exception ignored) {}
  }
}
