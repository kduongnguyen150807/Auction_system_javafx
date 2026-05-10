package com.auction.client.service;

import com.auction.client.ui.utils.RequestHelper;
import com.auction.shared.item.Item;
import com.auction.shared.linkv2.RequestType;

import java.util.concurrent.CompletableFuture;

public class AdminService {
  public CompletableFuture<Boolean> handleApprove(Item item) {
    RequestHelper.sendRequest(RequestType.APPROVE_ITEM, item);
    return CompletableFuture.completedFuture(true);
  }
}
