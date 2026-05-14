package com.auction.client.ui.utils;

import javafx.scene.control.TextField;

public class UIutils {
  public static void setNumericField(TextField textField) {
    textField.textProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal.matches("\\d*(\\.\\d*)?")) textField.setText(oldVal);
    });
  }
}
