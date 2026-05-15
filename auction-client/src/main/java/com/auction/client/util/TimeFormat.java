package com.auction.client.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeFormat {
  private TimeFormat() {}

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
