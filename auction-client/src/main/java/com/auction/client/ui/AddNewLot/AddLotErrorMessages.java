package com.auction.client.ui.AddNewLot;

/** Maps server lot-submission error codes to Vietnamese UI text. */
final class AddLotErrorMessages {

  private AddLotErrorMessages() {}

  static String format(String code) {
    if (code == null || code.isBlank()) {
      return "Thao tác thất bại";
    }
    return switch (code) {
      case "end_time_too_early_for_dutch_drops" ->
          "Thời gian kết thúc quá sớm — không đủ chỗ cho tất cả lần giảm giá.";
      case "dutch_window_too_short" ->
          "Khoảng start–end quá ngắn so với interval. Kéo dài end hoặc giảm interval.";
      case "invalid_time_range" -> "Thời gian kết thúc phải sau thời gian bắt đầu.";
      case "Reserve must be below starting price" -> "Giá reserve phải thấp hơn giá khởi điểm.";
      case "Price decrement must be positive" -> "Bước giảm giá phải lớn hơn 0.";
      case "Decrease interval must be at least 1 minute" -> "Interval phải ít nhất 1 phút.";
      case "Invalid reserve price" -> "Giá reserve không hợp lệ.";
      default -> code;
    };
  }
}
