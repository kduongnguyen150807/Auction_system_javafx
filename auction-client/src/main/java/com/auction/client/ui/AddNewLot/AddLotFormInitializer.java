package com.auction.client.ui.AddNewLot;

import java.time.LocalDate;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

/** One-time setup of add-lot form controls (combos, defaults, auction kind toggle). */
final class AddLotFormInitializer {

  private AddLotFormInitializer() {}

  static void setupCategoryCombo(ComboBox<String> classifyComboBox) {
    classifyComboBox.setPromptText("CATEGORY");
    classifyComboBox.setEditable(false);
    classifyComboBox.getItems().addAll("Electronics", "Art", "Vehicle");
    classifyComboBox.setButtonCell(
        new ListCell<>() {
          @Override
          protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(item == null ? "CATEGORY" : item);
          }
        });
  }

  static void setupTimeCombos(
      ComboBox<Integer> startHourCombo,
      ComboBox<Integer> startMinuteCombo,
      ComboBox<Integer> startSecondCombo,
      ComboBox<Integer> endHourCombo,
      ComboBox<Integer> endMinuteCombo,
      ComboBox<Integer> endSecondCombo,
      DatePicker startDatePicker,
      DatePicker endDatePicker) {
    for (int i = 0; i < 24; i++) {
      startHourCombo.getItems().add(i);
      endHourCombo.getItems().add(i);
    }
    for (int i = 0; i < 60; i++) {
      startMinuteCombo.getItems().add(i);
      startSecondCombo.getItems().add(i);
      endMinuteCombo.getItems().add(i);
      endSecondCombo.getItems().add(i);
    }
    startHourCombo.setValue(0);
    startMinuteCombo.setValue(0);
    startSecondCombo.setValue(0);
    endHourCombo.setValue(23);
    endMinuteCombo.setValue(59);
    endSecondCombo.setValue(0);
    startDatePicker.setValue(LocalDate.now());
    endDatePicker.setValue(LocalDate.now().plusDays(1));
  }

  static ToggleGroup setupAuctionKindToggle(
      ToggleButton kindEnglishToggle, ToggleButton kindDutchToggle, Runnable onKindChanged) {
    ToggleGroup group = new ToggleGroup();
    kindEnglishToggle.setToggleGroup(group);
    kindDutchToggle.setToggleGroup(group);
    kindEnglishToggle.setSelected(true);
    group.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> onKindChanged.run());
    return group;
  }
}
