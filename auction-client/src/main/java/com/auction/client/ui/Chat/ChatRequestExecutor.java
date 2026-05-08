package com.auction.client.ui.Chat;

import com.auction.client.network.NetworkClient;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.function.Consumer;

/** Runs {@link Request}s off the JavaFX thread. */
final class ChatRequestExecutor {

  private ChatRequestExecutor() {}

  static void submitAsync(Request req, Consumer<Response> callback) {
    Thread t =
        new Thread(
            () -> {
              Response res = NetworkClient.getInstance().sendRequestAndWait(req);
              if (res != null) {
                callback.accept(res);
              }
            });
    t.setDaemon(true);
    t.start();
  }
}
