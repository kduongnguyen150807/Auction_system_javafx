package com.auction.client.service.auction;

import com.auction.client.util.RequestHelper;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LotManagementService {

  public CompletableFuture<Response> registerLot(Map<String, String> lotForm) {
    if (lotForm == null || lotForm.isEmpty()) {
      return CompletableFuture.failedFuture(new IllegalArgumentException("Dữ liệu form đăng ký không được để trống"));
    }

    return RequestHelper.sendRequest(Request.ADD_LOT, lotForm);
  }
}