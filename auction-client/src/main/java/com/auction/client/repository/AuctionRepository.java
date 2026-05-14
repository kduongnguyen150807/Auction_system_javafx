package com.auction.client.repository;

import com.auction.client.ui.maindashboard.registerlot.LotForm;
import com.auction.client.ui.utils.RequestHelper;
import com.auction.shared.dto.BidForm;
import com.auction.shared.item.Item;
import com.auction.shared.linkv2.RequestType;
import com.auction.shared.linkv2.Response;
import com.auction.shared.linkv2.ResponseStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AuctionRepository {
  public CompletableFuture<List<Item>> getAllItems() {
    return RequestHelper.<List<Item>>sendRequest(RequestType.GET_ALL_ITEMS, null)
      .thenApply(Response::getData);
  }

  public CompletableFuture<ResponseStatus> registerLot(Item item) {
    return RequestHelper.sendRequest(RequestType.REGISTER_LOT, item)
      .thenApply(Response::getStatus);
  }

  public CompletableFuture<ResponseStatus> placeBid(BidForm bidForm) {
    return RequestHelper.sendRequest(RequestType.PLACE_BID, bidForm)
      .thenApply(Response::getStatus);
  }
}
