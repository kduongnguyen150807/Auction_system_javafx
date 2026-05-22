package com.auction.shared;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class SpinWheelState implements Serializable {
  private static final long serialVersionUID = 1L;

  public static final double SPIN_CREDIT_PRICE = 5.0;

  private boolean freeSpinAvailable;
  private LocalDateTime nextFreeSpinAt;
  private int paidSpinCredits;
  private double spinCreditPrice;
  private List<SpinWheelSegmentInfo> segments;

  public SpinWheelState() {
    this.spinCreditPrice = SPIN_CREDIT_PRICE;
    this.segments = SpinWheelSegment.allSegments();
  }

  public boolean isFreeSpinAvailable() {
    return freeSpinAvailable;
  }

  public void setFreeSpinAvailable(boolean freeSpinAvailable) {
    this.freeSpinAvailable = freeSpinAvailable;
  }

  public LocalDateTime getNextFreeSpinAt() {
    return nextFreeSpinAt;
  }

  public void setNextFreeSpinAt(LocalDateTime nextFreeSpinAt) {
    this.nextFreeSpinAt = nextFreeSpinAt;
  }

  public int getPaidSpinCredits() {
    return paidSpinCredits;
  }

  public void setPaidSpinCredits(int paidSpinCredits) {
    this.paidSpinCredits = paidSpinCredits;
  }

  public double getSpinCreditPrice() {
    return spinCreditPrice;
  }

  public void setSpinCreditPrice(double spinCreditPrice) {
    this.spinCreditPrice = spinCreditPrice;
  }

  public List<SpinWheelSegmentInfo> getSegments() {
    return segments;
  }

  public void setSegments(List<SpinWheelSegmentInfo> segments) {
    this.segments = segments;
  }
}
