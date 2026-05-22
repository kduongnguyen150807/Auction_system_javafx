package com.auction.shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SpinWheelResult implements Serializable {
  private static final long serialVersionUID = 1L;

  private int segmentIndex;
  private String prizeLabel;
  private String message;
  private User user;
  private boolean freeSpinAvailable;
  private LocalDateTime nextFreeSpinAt;
  private int paidSpinCredits;

  public int getSegmentIndex() {
    return segmentIndex;
  }

  public void setSegmentIndex(int segmentIndex) {
    this.segmentIndex = segmentIndex;
  }

  public String getPrizeLabel() {
    return prizeLabel;
  }

  public void setPrizeLabel(String prizeLabel) {
    this.prizeLabel = prizeLabel;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
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
}
