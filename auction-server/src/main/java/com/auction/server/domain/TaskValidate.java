package com.auction.server.domain;

import com.auction.shared.dto.BidForm;

public class TaskValidate {
  public static boolean validate(BidForm bidForm) {
    if (bidForm == null) {
      return false;
    }
    if (bidForm.getBidAmount() <= 0) {
      return false;
    }

    return true;
  }
}
