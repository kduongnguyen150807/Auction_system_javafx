package com.auction.client.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeFormat {
  private TimeFormat() {
  }

  public static DateTimeFormatter DMY_HM =
    DateTimeFormatter.ofPattern(
      "dd/MM/yyyy HH:mm"
    );

  public static final DateTimeFormatter DMY_HMS =
    DateTimeFormatter.ofPattern(
      "dd/MM/yyyy HH:mm:ss"
    );

  public static String getDHMS(LocalDateTime time) {
    Duration duration = Duration.between(LocalDateTime.now(), time);
    long days = duration.toDays();
    long hours = duration.toHours() % 24;
    long minutes = duration.toMinutes() % 60;
    long seconds = duration.getSeconds() % 60;

    String text = String.format(
      "%dd %dh %dm %s",
      days,
      hours,
      minutes,
      seconds
    );
    return text;
  }

  public static String getUpcommingDHMS(LocalDateTime time) {
    Duration duration = Duration.between(time, LocalDateTime.now());
    long days = duration.toDays();
    long hours = duration.toHours() % 24;
    long minutes = duration.toMinutes() % 60;
    long seconds = duration.getSeconds() % 60;

    String text = String.format(
      "%dd %dh %dm %s",
      days,
      hours,
      minutes,
      seconds
    );
    return text;
  }

  public static String getDHM(LocalDateTime time) {
    Duration duration = Duration.between(LocalDateTime.now(), time);
    long days = duration.toDays();
    long hours = duration.toHours() % 24;
    long minutes = duration.toMinutes() % 60;

    String text = String.format(
      "%dd %dh %dm",
      days,
      hours,
      minutes
    );
    return text;
  }

  public static String buildDateTime(LocalDate date, Integer h, Integer m, Integer s) {
    if (date == null || h == null || m == null || s == null) return null;
    try {
      return LocalDateTime.of(date, LocalTime.of(h, m, s)).format(DMY_HMS);
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  public static LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return LocalDateTime.parse(value, DMY_HMS);
    } catch (DateTimeParseException e) {
      return null;
    }
  }
}
