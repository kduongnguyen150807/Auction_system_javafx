package com.auction.shared.link;

import java.time.LocalDateTime;

public class ErrorResponse extends Response {
  private static final String ERROR_STATUS = Response.ERROR;
  private static final String REQUEST_ID = "";

  public ErrorResponse(String msg) {
    super(REQUEST_ID, ERROR_STATUS, msg, null);
    this.timestamp = LocalDateTime.now();
  }
}