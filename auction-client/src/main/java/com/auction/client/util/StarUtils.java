package com.auction.client.util;

public class StarUtils {
  private StarUtils() {}

  public static String stars(double rating) {

    int full =
      (int) rating;

    boolean half =
      rating - full >= 0.5;

    int empty =
      5 - full - (half ? 1 : 0);

    return "★".repeat(full)
      + (half ? "⯨" : "")
      + "☆".repeat(empty);
  }
}
