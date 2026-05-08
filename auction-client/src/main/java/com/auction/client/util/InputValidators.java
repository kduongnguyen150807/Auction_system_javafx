package com.auction.client.util;

import java.util.regex.Pattern;

/** Pure input checks shared by controllers (no JavaFX, no network). */
public final class InputValidators {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

  private InputValidators() {}

  public static boolean isValidEmail(String email) {
    return email != null && !email.trim().isEmpty() && EMAIL_PATTERN.matcher(email).matches();
  }
}
