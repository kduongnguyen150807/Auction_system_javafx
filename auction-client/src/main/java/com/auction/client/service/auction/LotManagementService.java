package com.auction.client.service.auction;

import com.auction.client.util.RequestHelper;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.Map;

public class LotManagementService {
  public String registerLot(Map<String, String> lotForm) {
    return RequestHelper.sendRequest(Request.ADD_LOT, lotForm)
      .thenApply(response -> response.getStatus().equals(Response.OK) ? null : response.getMessage())
      .join();
  }
}