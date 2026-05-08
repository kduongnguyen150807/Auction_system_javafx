package com.auction.client.service;

import com.auction.client.ui.maindashboard.registerlot.LotForm;
import com.auction.client.ui.maindashboard.registerlot.LotMapper;
import com.auction.client.ui.utils.RequestHelper;
import com.auction.shared.linkv2.RequestType;
import com.auction.shared.linkv2.Response;
import com.auction.shared.linkv2.ResponseStatus;

import java.util.function.Consumer;

public final class AuctionService {
  private static AuctionService instance;
  private final LotMapper lotMapper;

  private AuctionService() {
    lotMapper = new LotMapper();
  }

  public static AuctionService getInstance() {
    if (instance == null) {
      instance = new AuctionService();
    }
    return instance;
  }

  public void registerLot(LotForm lotForm, Consumer<Response<Object>> callback, Consumer<Throwable> errorCallback) {
    RequestHelper.sendRequest(RequestType.REGISTER_LOT, lotMapper.map(lotForm),
      response -> {
        handleRegisterResponse((Response<Object>) response, callback);
      }, errorCallback);
  }

  private void handleRegisterResponse(Response<Object> response, Consumer<Response<Object>> callback) {
    callback.accept(response);
  }
}
