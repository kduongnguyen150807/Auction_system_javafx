package com.auction.client.ui.utils;

import com.auction.client.network.NetworkClient;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.RequestType;
import com.auction.shared.linkv2.Response;

import java.util.concurrent.CompletableFuture;

public final class RequestHelper {

  public static <T> CompletableFuture<Response<T>> sendRequest(RequestType type, Object data) {
    Request request = new Request(type, data);
    return NetworkClient.getInstance().sendRequestAsync(request)
      .thenApply(response -> (Response<T>) response);
  }
}