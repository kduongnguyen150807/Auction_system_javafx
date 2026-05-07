package com.auction.client.ui.utils;

import com.auction.client.network.NetworkClient;
import com.auction.shared.link.Request;
import com.auction.shared.link.Response;

import java.util.function.Consumer;


/**
 * Utility hỗ trợ gửi request bất đồng bộ đến server.
 *
 * <p>Class này đóng vai trò wrapper cho {@link NetworkClient}
 * nhằm giảm lặp code xử lý CompletableFuture trong Controller.
 */
public final class RequestHelper {

  /**
   * Gửi request bất đồng bộ đến server.
   *
   * <p>Callback sẽ tự động được đưa về JavaFX UI Thread.
   *
   * @param request request cần gửi
   * @param responseConsumer callback khi thành công
   * @param errorConsumer callback khi thất bại
   */
  public static void sendRequest(Request request, Consumer<Response> responseConsumer, Consumer<Throwable> errorConsumer) {
    NetworkClient.getInstance().sendRequestAsync(request)
      .thenAccept(responseConsumer).exceptionally(ex -> {
        errorConsumer.accept(ex);
        return null;
      });
  }
}
