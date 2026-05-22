package com.auction.shared;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/** Wheel segments in clockwise order (index = wheel position). */
public enum SpinWheelSegment {
  VIP_1_DAY(0, "VIP 1 ngày", 5_000),
  VIP_2_DAYS(1, "VIP 2 ngày", 3_000),
  VIP_1_YEAR(2, "VIP 1 năm", 10),
  BETTER_LUCK(3, "Chúc bạn may mắn lần sau", 81_980),
  CASH_10(4, "10$", 8_000),
  CASH_100(5, "100$", 2_000),
  CASH_10000(6, "10000$", 10);

  private static final int TOTAL_WEIGHT =
      Arrays.stream(values()).mapToInt(SpinWheelSegment::getWeight).sum();

  private final int index;
  private final String label;
  private final int weight;

  SpinWheelSegment(int index, String label, int weight) {
    this.index = index;
    this.label = label;
    this.weight = weight;
  }

  public int getIndex() {
    return index;
  }

  public String getLabel() {
    return label;
  }

  public int getWeight() {
    return weight;
  }

  public static SpinWheelSegment fromIndex(int idx) {
    return Arrays.stream(values()).filter(s -> s.index == idx).findFirst().orElse(BETTER_LUCK);
  }

  public static List<SpinWheelSegmentInfo> allSegments() {
    return Arrays.stream(values()).map(SpinWheelSegment::toInfo).toList();
  }

  public SpinWheelSegmentInfo toInfo() {
    return new SpinWheelSegmentInfo(index, label);
  }

  /** Weighted random pick; VIP 1 năm / 10000$ ≈ 0.01% each. */
  public static SpinWheelSegment roll(java.util.Random random) {
    int pick = random.nextInt(TOTAL_WEIGHT);
    int cumulative = 0;
    for (SpinWheelSegment segment : values()) {
      cumulative += segment.weight;
      if (pick < cumulative) {
        return segment;
      }
    }
    return BETTER_LUCK;
  }
}
