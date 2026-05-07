package com.auction.client.ui.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.IntStream;

public class TimeUI {
  public static final ObservableList<Integer> HOURS = FXCollections.observableArrayList(
    IntStream.range(0, 24).boxed().toList()
  );

  public static final ObservableList<Integer> MINS_SECS = FXCollections.observableArrayList(
    IntStream.range(0, 60).boxed().toList()
  );

  public static LocalDateTime combine(DatePicker dp, ComboBox<Integer> h, ComboBox<Integer> m, ComboBox<Integer> s) {
    if (dp.getValue() == null || h.getValue() == null) return null;
    return LocalDateTime.of(dp.getValue(), LocalTime.of(h.getValue(), m.getValue(), s.getValue()));
  }

  private TimeUI() {}


}
