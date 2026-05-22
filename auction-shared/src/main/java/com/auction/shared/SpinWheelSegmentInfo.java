package com.auction.shared;

import java.io.Serializable;

public class SpinWheelSegmentInfo implements Serializable {
  private static final long serialVersionUID = 1L;

  private int index;
  private String label;

  public SpinWheelSegmentInfo() {}

  public SpinWheelSegmentInfo(int index, String label) {
    this.index = index;
    this.label = label;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
}
