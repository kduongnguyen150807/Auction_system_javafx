package com.auction.client.ui.AddNewLot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Parses and formats auction start/end timestamps for the add-lot form. */
final class AddLotDateTimeHelper {

  static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

  private AddLotDateTimeHelper() {}

  static String buildNormalized(LocalDate date, Integer hour, Integer minute, Integer second) {
    if (date == null || hour == null || minute == null || second == null) {
      return null;
    }
    try {
      return LocalDateTime.of(date, LocalTime.of(hour, minute, second)).format(FORMAT);
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  static LocalDateTime parseNormalized(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return LocalDateTime.parse(value, FORMAT);
    } catch (DateTimeParseException e) {
      return null;
    }
  }
}
