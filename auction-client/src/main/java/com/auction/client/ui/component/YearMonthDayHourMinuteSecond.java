package com.auction.client.ui.component;

import com.auction.client.util.TimeFormat;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class YearMonthDayHourMinuteSecond extends VBox {
  private static final String BASE_FXML_PATH = "/fxml/component/YearMonthDayHourMinuteSecond.fxml";

  public SimpleStringProperty title =  new SimpleStringProperty();

  private Map<String, Integer> defaultValue = new HashMap<>();

  @FXML private Label titleLabel;

  @FXML private DatePicker datePicker;
  @FXML private ComboBox<Integer> hourCombo;
  @FXML private ComboBox<Integer> minuteCombo;
  @FXML private ComboBox<Integer> secondCombo;

  public YearMonthDayHourMinuteSecond() {
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
  public void initialize() {
    titleLabel.textProperty().bind(title);
    setUpUI();
  }

  public void resetToDefault() {
    hourCombo.setValue(defaultValue.get("hour"));
    minuteCombo.setValue(defaultValue.get("minute"));
    secondCombo.setValue(defaultValue.get("second"));

    datePicker.setValue(java.time.LocalDate.now());
  }

  public void setDefaultValues(int hour, int minute, int second) {
    hourCombo.setValue(hour);
    minuteCombo.setValue(minute);
    secondCombo.setValue(second);

    defaultValue.clear();
    defaultValue.put("hour", hour);
    defaultValue.put("minute", minute);
    defaultValue.put("second", second);
  }

  private void setUpUI() {
    for (int i = 0; i < 24; i++) {
      hourCombo.getItems().add(i);
    }

    for (int i = 0; i < 60; i++) {
      minuteCombo.getItems().add(i);
      secondCombo.getItems().add(i);
    }

    datePicker.setValue(java.time.LocalDate.now());

    hourCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? "HH" : String.format("%02d", item));
      }
    });

    minuteCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? "MM" : String.format("%02d", item));
      }
    });

    secondCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? "SS" : String.format("%02d", item));
      }
    });

    hourCombo.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : String.format("%02d", item));
      }
    });

    minuteCombo.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : String.format("%02d", item));
      }
    });

    secondCombo.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : String.format("%02d", item));
      }
    });

    hourCombo.setPromptText("HH");
    minuteCombo.setPromptText("MM");
    secondCombo.setPromptText("SS");

    datePicker.setPromptText("SELECT DATE");
  }

  public String collectData() {
    return TimeFormat.buildDateTime(
      datePicker.getValue(),
      hourCombo.getValue(),
      minuteCombo.getValue(),
      secondCombo.getValue()
    );
  }

  public LocalDateTime collectDataInLocalDateTime() {
    String time =  TimeFormat.buildDateTime(
      datePicker.getValue(),
      hourCombo.getValue(),
      minuteCombo.getValue(),
      secondCombo.getValue()
    );

    return TimeFormat.parseDateTime(time);
  }

  public SimpleStringProperty titleProperty() {
    return title;
  }

  public void setTitle(String title) {
    this.title.set(title);
  }

  public String getTitle() {
    return title.get();
  }
}
