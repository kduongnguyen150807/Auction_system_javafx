package com.auction.client.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeFormat {
  private TimeFormat() {}

  public static DateTimeFormatter DMY_HM =
    DateTimeFormatter.ofPattern(
      "dd/MM/yyyy HH:mm"
    );

  public static String getRemainingTime(LocalDateTime time) {
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
}
