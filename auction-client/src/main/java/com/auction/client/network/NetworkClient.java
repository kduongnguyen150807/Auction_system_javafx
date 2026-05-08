package com.auction.client.network;

import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.Response;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Lớp điều khiển kết nối mạng phía Client (Singleton).
 * <p>
 * Chịu trách nhiệm thiết lập kết nối Socket tới Server, gửi các đối tượng {@link Request}
 * và nhận phản hồi {@link Response} một cách bất đồng bộ.
 * </p>
 *
 */
public class NetworkClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(NetworkClient.class);
  private static final long DEFAULT_REQUEST_TIMEOUT_SECONDS = 30L;
  private static volatile NetworkClient instance;


  private Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;
  /**
   * Bản đồ lưu trữ các yêu cầu đang chờ phản hồi từ Server
   */
  private final ConcurrentHashMap<String, CompletableFuture<Response<?>>> pendingMap = new ConcurrentHashMap<>();

  private NetworkClient() {}

  /**
   * Kiểm tra trạng thái kết nối
   */
  public boolean isConnected() {
    return socket != null && !socket.isClosed() && socket.isConnected();
  }

  public boolean connect() {
    if (isConnected()) return true;

    String serverIp = promptForServerIp();
    if (serverIp == null || serverIp.isBlank()) return false;

    try {
      LOGGER.info("Connecting to {}:8080 ...", serverIp);
      this.socket = new Socket(serverIp, 8080);
      this.out = new ObjectOutputStream(this.socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.socket.getInputStream());
      LOGGER.info("Socket connection established.");

      startListener();
      return true;
    } catch (Exception e) {
      LOGGER.error("Connection to {}:8080 failed", serverIp, e);
      return false;
    }
  }

  private String promptForServerIp() {
    // Nếu không phải UI Thread, chúng ta phải dùng Future để lấy kết quả từ Platform.runLater
    if (!Platform.isFxApplicationThread()) {
      CompletableFuture<String> resultFuture = new CompletableFuture<>();
      Platform.runLater(() -> resultFuture.complete(showIpDialog()));
      try {
        return resultFuture.get(1, TimeUnit.MINUTES);
      } catch (Exception e) {
        return null;
      }
    }
    return showIpDialog();
  }

  private String showIpDialog() {
    TextInputDialog dialog = new TextInputDialog("127.0.0.1");
    dialog.setTitle("Server IP");
    dialog.setHeaderText("Enter Server IP address:");
    Optional<String> result = dialog.showAndWait();
    return result.orElse(null);
  }

  /**
   * Lấy instance duy nhất của NetworkClient (Double-checked locking).
   *
   * @return Đối tượng NetworkClient.
   */
  public static NetworkClient getInstance() {
    if (instance == null) synchronized (NetworkClient.class) {
      if (instance == null) instance = new NetworkClient();
    }
    return instance;
  }

  /**
   * Bắt đầu luồng lắng nghe dữ liệu từ Server liên tục.
   */
  private void startListener() {
    Thread t = new Thread(() -> {
      try {
        while (true) {
          Object obj = in.readObject();
          if (obj instanceof Response<?> response) handleIncoming(response);
        }
      } catch (Exception e) {
        LOGGER.warn("Server connection lost", e);
      }
    }, "NetworkClient-Listener");
    t.setDaemon(true);
    t.start();
  }

  /**
   * Khớp phản hồi nhận được với yêu cầu đang chờ trong Map.
   */
  private void handleIncoming(Response<?> response) {
    String requestId = response.getId();
    if (requestId == null) return;
    CompletableFuture<Response<?>> future = pendingMap.get(requestId);
    LOGGER.info("Received request {} from {}", requestId, response.getMessage());
    if (future != null) future.complete(response);
    else LOGGER.warn("Received response for unknown request ID: {}", requestId);
  }

  /**
   * Gửi yêu cầu bất đồng bộ tới Server.
   *
   * @param request Đối tượng yêu cầu.
   * @return Một {@link CompletableFuture} chứa phản hồi trong tương lai.
   */
  public CompletableFuture<Response<?>> sendRequestAsync(Request<?> request) {
    String requestId = request.getId();
    CompletableFuture<Response<?>> future = new CompletableFuture<>();
    if (out == null) {
      future.completeExceptionally(new IllegalStateException(
        "Not connected to server — check IP address and server status."));
      return future;
    }
    CompletableFuture<Response<?>> existing = pendingMap.putIfAbsent(requestId, future);
    if (existing != null) {
      future.completeExceptionally(new IllegalStateException("Duplicate request id: " + requestId));
      return future;
    }
    try {
      synchronized (out) {
        out.writeObject(request);
        out.flush();
      }
    } catch (IOException e) {
      pendingMap.remove(requestId);
      future.completeExceptionally(e);
    }
    return future.orTimeout(DEFAULT_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
      .whenComplete((response, error) -> {
        pendingMap.remove(requestId);
        if (error != null) {
          if (error instanceof java.util.concurrent.TimeoutException)
            LOGGER.warn("Server timeout ({}s) for request {} ({})", DEFAULT_REQUEST_TIMEOUT_SECONDS, requestId, request.getType());
          else
            LOGGER.warn("Request {} ({}) failed: {}", requestId, request.getType(), error.getMessage());
        }
      });
  }

  /**
   * Gửi yêu cầu và đợi phản hồi (Chặn luồng hiện tại).
   *
   * @param request Đối tượng yêu cầu.
   * @return Phản hồi từ Server hoặc null nếu lỗi/timeout.
   */
  public Response<?> sendRequestAndWait(Request<?> request) {
    try {
      LOGGER.info("Sending request: {} with id {}", request.getType(),  request.getId());
      return sendRequestAsync(request).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.warn("Interrupted while waiting for response to {}", request.getType());
      return null;
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof TimeoutException) return null;
      LOGGER.error("Socket send/receive error", cause);
      return null;
    }
  }
}
