package com.auction.client.util;

import java.util.List;

public class StarUtils {
  private StarUtils() {}

  public static List<String> ratingList = List.of("All", "Positive", "Neutral", "Negative", "No Rating");

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


  public static String getRatingTypeFromAvg(double avgRating) {
    if (avgRating <= 0.0) {
      return "No Rating";
    }

    if (avgRating >= 4.0) {
      return "Positive";
    }

    if (avgRating >= 3.0) {
      return "Neutral";
    }

    return "Negative";
  }
}
