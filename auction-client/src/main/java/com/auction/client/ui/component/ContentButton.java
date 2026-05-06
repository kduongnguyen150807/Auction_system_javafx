package com.auction.client.ui.component;

import javafx.scene.control.Button;

public class ContentButton<T> extends Button {
  private final T type;
  public ContentButton(String text, T type) {
    super(text);
    this.type = type;
    this.getStylesheets().add(ContentButton.class.getResource("/css/Component/ContentButton.css").toExternalForm());
    this.getStyleClass().addAll("nav-button", "nav-button-inactive");
    this.setPrefWidth(200);
  }

  public T getType() {
    return type;
  }
}
