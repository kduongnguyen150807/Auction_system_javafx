package com.auction.client.ui.AddNewLot;

/** Best-effort parsing of optional numeric form fields. */
final class AddLotFormParseHelper {

  private AddLotFormParseHelper() {}

  static Double optionalDouble(String raw) {
    if (raw == null) {
      return null;
    }
    String trimmed = raw.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    try {
      return Double.parseDouble(trimmed);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  static Integer optionalInt(String raw) {
    if (raw == null) {
      return null;
    }
    String trimmed = raw.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    try {
      return Integer.parseInt(trimmed);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
