package com.auction.client.ui.utils;

import com.auction.client.network.NetworkClient;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.RequestType;
import com.auction.shared.linkv2.Response;
import javafx.application.Platform;

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
   * @param type hành động yêu cầu
   * @param payload gói thông tin cần gửi
   * @param responseConsumer callback khi thành công
   * @param errorConsumer callback khi thất bại
   */
  public static <T> void sendRequest(RequestType type, Object payload, Consumer<Response<T>> responseConsumer, Consumer<Throwable> errorConsumer) {
    Request request = Request.of(type, payload);
    NetworkClient.getInstance().sendRequestAsync(request)
      .thenAccept(response -> {
        Platform.runLater(() -> {responseConsumer.accept((Response<T>) response);});
      }).exceptionally(ex -> {
        Platform.runLater(() -> {errorConsumer.accept(ex);});
        return null;
      });
  }

  public static void sendRequest(RequestType type, Object payload) {
    Request request = Request.of(type, payload);
    NetworkClient.getInstance().sendRequestAsync(request);
  }
}
