package com.auction.client.ui.Chat;

import com.auction.client.network.NetworkClient;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.function.Consumer;

/** Runs blocking socket requests on a daemon thread and delivers responses on the caller thread (via callback). */
final class ChatAsyncRequests {

  private ChatAsyncRequests() {}

  static void submit(Request req, Consumer<Response> callback) {
    Thread t =
        new Thread(
            () -> {
              Response res = NetworkClient.getInstance().sendRequestAndWait(req);
              if (res != null) callback.accept(res);
            });
    t.setDaemon(true);
    t.start();
  }
}
