package com.auction.shared;

import java.util.Arrays;
import java.util.List;

/** Canonical VIP subscription tiers (shared client/server). */
public enum VipPlan {
  DAYS_7("7d", 7, 10, "7 ngày"),
  MONTH_1("1m", 30, 30, "1 tháng"),
  MONTH_3("3m", 90, 75, "3 tháng"),
  YEAR_1("1y", 365, 250, "1 năm");

  private final String id;
  private final int days;
  private final double price;
  private final String label;

  VipPlan(String id, int days, double price, String label) {
    this.id = id;
    this.days = days;
    this.price = price;
    this.label = label;
  }

  public String getId() {
    return id;
  }

  public int getDays() {
    return days;
  }

  public double getPrice() {
    return price;
  }

  public String getLabel() {
    return label;
  }

  public VipPlanInfo toInfo() {
    return new VipPlanInfo(id, label, days, price);
  }

  public static VipPlan fromId(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    String key = raw.trim().toLowerCase();
    return Arrays.stream(values()).filter(p -> p.id.equals(key)).findFirst().orElse(null);
  }

  public static List<VipPlanInfo> allPlans() {
    return Arrays.stream(values()).map(VipPlan::toInfo).toList();
  }
}
