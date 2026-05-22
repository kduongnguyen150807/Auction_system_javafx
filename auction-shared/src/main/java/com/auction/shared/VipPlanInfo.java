package com.auction.shared;

import java.io.Serializable;

public class VipPlanInfo implements Serializable {
  private static final long serialVersionUID = 1L;

  private String id;
  private String label;
  private int days;
  private double price;

  public VipPlanInfo() {}

  public VipPlanInfo(String id, String label, int days, double price) {
    this.id = id;
    this.label = label;
    this.days = days;
    this.price = price;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public int getDays() {
    return days;
  }

  public void setDays(int days) {
    this.days = days;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }
}
