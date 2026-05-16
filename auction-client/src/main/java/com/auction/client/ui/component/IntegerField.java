package com.auction.client.ui.component;

import javafx.scene.control.TextField;

public class IntegerField extends TextField {
  @Override
  public void replaceText(int start, int end, String text) {

    if (text.matches("[0-9]*")) {
      super.replaceText(start, end, text);
    }
  }

  @Override
  public void replaceSelection(String text) {

    if (text.matches("[0-9]*")) {
      super.replaceSelection(text);
    }
  }

  public int getValue() {
    return getText().isBlank() ? -1 : Integer.parseInt(getText());
  }
}
