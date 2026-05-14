package com.auction.shared;

import java.io.Serializable;

public class ResultBase implements Serializable {
  private final boolean success;
  private final String message;

  public ResultBase(boolean success, String message) {
    this.success = success;
    this.message = message;
  }
  public boolean isSuccess() {
    return success;
  }

  public String getMessage() { return message; }
}
