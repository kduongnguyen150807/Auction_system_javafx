package com.auction.client.ui.component;

import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MetricCard extends VBox {
  private static final String BASE_FXML_PATH = "/fxml/component/MetricCard.fxml";

  @FXML private Label titleLabel;
  @FXML private Label valueLabel;

  private final StringProperty title = new SimpleStringProperty();

  public MetricCard() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    loader.setController(this);
    loader.setRoot(this);
    try {
      loader.load();
    }  catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @FXML
  private void initialize() {
    titleLabel.textProperty().bind(title);
  }

  public void unbind() {
    valueLabel.textProperty().unbind();
  }

  public void bind(ObservableValue<String> value) {
    unbind();
    valueLabel.textProperty().bind(value);
  }

  public void setText(String text) {
    valueLabel.setText(text);
  }

  public StringProperty titleProperty() {
    return title;
  }

  public String getTitle() {
    return title.get();
  }

  public void setTitle(String value) {
    title.set(value);
  }
}
