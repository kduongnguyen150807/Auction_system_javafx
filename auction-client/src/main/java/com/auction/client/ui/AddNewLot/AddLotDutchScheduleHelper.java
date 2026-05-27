package com.auction.client.ui.AddNewLot;

import com.auction.shared.DutchAuctionPricing;
import java.time.LocalDateTime;

/** Builds Dutch schedule preview text and validates interval-driven schedules. */
final class AddLotDutchScheduleHelper {

  record Preview(String text, String cssStyle, Double derivedTick) {
    static Preview empty() {
      return new Preview("", "", null);
    }

    static Preview hint(String message) {
      return new Preview(message, "", null);
    }

    static Preview warning(String message) {
      return new Preview(message, "-fx-text-fill: #ff6b6b;", null);
    }
  }

  record ValidationResult(String errorCode, double derivedTick) {
    static ValidationResult ok(double tick) {
      return new ValidationResult(null, tick);
    }

    static ValidationResult fail(String code) {
      return new ValidationResult(code, 0);
    }

    boolean ok() {
      return errorCode == null;
    }
  }

  private AddLotDutchScheduleHelper() {}

  static Preview buildPreview(
      LocalDateTime start,
      LocalDateTime end,
      double ceiling,
      double reserve,
      int interval) {
    if (start == null || end == null) {
      return Preview.hint("Chọn start, end, interval và reserve — bước giảm sẽ được tính tự động.");
    }
    if (!end.isAfter(start)) {
      return Preview.warning("⚠ Thời gian kết thúc phải sau thời gian bắt đầu.");
    }
    double tick = DutchAuctionPricing.derivedTickAmount(start, end, interval, ceiling, reserve);
    if (tick <= 0) {
      return Preview.warning(
          "⚠ Khoảng start–end quá ngắn so với interval — kéo dài end hoặc giảm interval.");
    }
    long drops = DutchAuctionPricing.dropSlotsBetween(start, end, interval);
    LocalDateTime lastDrop = start.plusMinutes(drops * interval);
    double finalPrice = Math.max(reserve, ceiling - drops * tick);
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("• %d lần giảm, mỗi %d phút", drops, interval));
    sb.append(String.format("%n• Bước giảm: $%.2f (tự tính)", tick));
    sb.append(String.format("%n• Giảm lần cuối: %s", lastDrop.format(AddLotDateTimeHelper.FORMAT)));
    sb.append(String.format("%n• Giá sau lần giảm cuối: $%.2f", finalPrice));
    return new Preview(sb.toString(), "", tick);
  }

  static ValidationResult validateForSubmit(
      LocalDateTime start,
      LocalDateTime end,
      double ceiling,
      double reserve,
      int interval) {
    double tick = DutchAuctionPricing.derivedTickAmount(start, end, interval, ceiling, reserve);
    if (tick <= 0) {
      return ValidationResult.fail("dutch_window_too_short");
    }
    String err =
        DutchAuctionPricing.validateDutchScheduleFromInterval(start, end, ceiling, reserve, interval);
    if (err != null) {
      return ValidationResult.fail(err);
    }
    return ValidationResult.ok(tick);
  }
}
