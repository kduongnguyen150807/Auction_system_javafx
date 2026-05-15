package com.auction.client.util;

import com.auction.client.network.NetworkClient;
import com.auction.shared.Request;
import com.auction.shared.Response;

import java.util.concurrent.CompletableFuture;

public class RequestHelper {
  public static CompletableFuture<Response> sendRequest(String action, Object payload) {
    CompletableFuture<Response> future = new CompletableFuture<>();
    NetworkClient client;
    try {
      client = NetworkClient.getInstance();
    } catch (Exception e) {
      future.complete(Response.error("", "cannot connect to server"));
      return future;
    }


    Request request = new Request(action, payload);
    Response res = client.sendRequestAndWait(request);
    future.complete(res);
    return future;
  }
}
