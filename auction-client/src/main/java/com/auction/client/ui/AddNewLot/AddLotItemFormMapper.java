package com.auction.client.ui.AddNewLot;

import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/** Reads/writes {@link Item} fields into add-lot form controls. */
final class AddLotItemFormMapper {

  record ImageTarget(Consumer<String> imageUrlConsumer, ImageView productImageView) {}

  private AddLotItemFormMapper() {}

  static void applyItem(
      Item item,
      TextField txtName,
      TextArea txtQuantity,
      TextField txtPrice,
      TextField txtMaxPrice,
      ToggleButton kindEnglishToggle,
      ToggleButton kindDutchToggle,
      TextField txtDutchReservePrice,
      TextField txtDutchIntervalMinutes,
      DatePicker startDatePicker,
      ComboBox<Integer> startHourCombo,
      ComboBox<Integer> startMinuteCombo,
      ComboBox<Integer> startSecondCombo,
      DatePicker endDatePicker,
      ComboBox<Integer> endHourCombo,
      ComboBox<Integer> endMinuteCombo,
      ComboBox<Integer> endSecondCombo,
      ComboBox<String> classifyComboBox,
      ImageTarget imageTarget) {
    txtName.setText(item.getName() != null ? item.getName() : "");
    txtQuantity.setText(item.getDescription() != null ? item.getDescription() : "");
    txtPrice.setText(Double.toString(item.getStartingPrice()));
    AuctionType type = item.getAuctionType();
    if (kindDutchToggle != null && type == AuctionType.DUTCH) {
      kindDutchToggle.setSelected(true);
      if (txtDutchReservePrice != null) {
        txtDutchReservePrice.setText(String.valueOf(item.getDutchReservePrice()));
      }
      if (txtDutchIntervalMinutes != null) {
        txtDutchIntervalMinutes.setText(String.valueOf(item.getDutchTickIntervalMinutes()));
      }
    } else if (kindEnglishToggle != null) {
      kindEnglishToggle.setSelected(true);
      double mx = item.getMaxPrice();
      txtMaxPrice.setText(mx > 0 ? Double.toString(mx) : "");
    }

    applyDateTime(
        item.getStartTime(),
        startDatePicker,
        startHourCombo,
        startMinuteCombo,
        startSecondCombo);
    applyDateTime(
        item.getEndTime(), endDatePicker, endHourCombo, endMinuteCombo, endSecondCombo);

    String cat = item.getCategory();
    if (cat != null && !cat.isBlank()) {
      if (!classifyComboBox.getItems().contains(cat)) {
        classifyComboBox.getItems().add(cat);
      }
      classifyComboBox.setValue(cat);
    }

    if (imageTarget != null) {
      String url = item.getImageUrl() != null ? item.getImageUrl() : "";
      if (imageTarget.imageUrlConsumer() != null) {
        imageTarget.imageUrlConsumer().accept(url);
      }
      if (!url.isBlank() && imageTarget.productImageView() != null) {
        imageTarget.productImageView().setImage(new Image(url, true));
      }
    }
  }

  private static void applyDateTime(
      LocalDateTime value,
      DatePicker datePicker,
      ComboBox<Integer> hourCombo,
      ComboBox<Integer> minuteCombo,
      ComboBox<Integer> secondCombo) {
    if (value == null) {
      return;
    }
    datePicker.setValue(value.toLocalDate());
    hourCombo.setValue(value.getHour());
    minuteCombo.setValue(value.getMinute());
    secondCombo.setValue(value.getSecond());
  }
}
