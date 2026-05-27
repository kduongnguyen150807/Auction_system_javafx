package com.auction.client.network;

import com.auction.shared.Request;
import com.auction.shared.Response;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkClient {

  private static final Logger logger = LoggerFactory.getLogger(NetworkClient.class);
  private static final long REQUEST_TIMEOUT = 30L;
  private static volatile NetworkClient instance;

  private final ObjectMapper jsonMapper;
  private final ConcurrentHashMap<String, CompletableFuture<Response>> pendingMap =
          new ConcurrentHashMap<>();
  private final List<NetworkEventListener> listeners = new CopyOnWriteArrayList<>();

  private DataOutputStream out;
  private String serverIp;

  private NetworkClient() {
    this.jsonMapper = new ObjectMapper();
    this.jsonMapper.registerModule(new JavaTimeModule());
    // BỎ QUA CÁC TRƯỜNG KHÔNG TỒN TẠI TRONG CLASS
    this.jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType(Object.class)
            .build();
    this.jsonMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

    NetworkConnectionUi ui = new NetworkConnectionUi();
    Optional<String> ipOpt = ui.promptForServerIp();
    if (ipOpt.isEmpty()) {
      return;
    }
    this.serverIp = ipOpt.get();
    initializeConnection();
  }

  private void initializeConnection() {
    try {
      ObjectSocketConnection conn = ObjectSocketConnection.connect(serverIp, 8080);
      this.out = conn.getOut();

      IncomingResponseRouter router = new IncomingResponseRouter(pendingMap, listeners);
      conn.startReadLoop(
              router::dispatch,
              error -> {
                this.out = null;
                failAllPending(error);
              });

      startHeartbeat();
    } catch (IOException e) {
      logger.error("Không thể kết nối tới server: {}", e.getMessage());
      new NetworkConnectionUi().showConnectionError(serverIp);
    }
  }

  public static NetworkClient getInstance() {
    if (instance == null) {
      synchronized (NetworkClient.class) {
        if (instance == null) {
          instance = new NetworkClient();
        }
      }
    }
    return instance;
  }

  public void addListener(NetworkEventListener l) {
    if (!listeners.contains(l)) {
      listeners.add(l);
    }
  }

  public void removeListener(NetworkEventListener l) {
    listeners.remove(l);
  }

  private void failAllPending(Throwable cause) {
    pendingMap.forEach((id, future) -> future.completeExceptionally(cause));
    pendingMap.clear();
  }

  // HÀM MỚI: Xử lý Async Timeout bằng CompletableFuture
  private CompletableFuture<Response> sendRequestWithFuture(Request request, long timeoutSeconds) {
    String requestId = request.getRequestId();
    CompletableFuture<Response> future = new CompletableFuture<>();
    if (out == null) {
      future.completeExceptionally(new IllegalStateException("Not connected to server"));
      return future;
    }

    CompletableFuture<Response> existing = pendingMap.putIfAbsent(requestId, future);
    if (existing != null) {
      future.completeExceptionally(new IllegalStateException("Duplicate request ID"));
      return future;
    }

    try {
      byte[] jsonBytes = jsonMapper.writeValueAsBytes(request);
      synchronized (out) {
        out.writeInt(jsonBytes.length);
        out.write(jsonBytes);
        out.flush();
      }
    } catch (Exception e) {
      pendingMap.remove(requestId);
      future.completeExceptionally(e);
    }

    return future.orTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .whenComplete((r, error) -> pendingMap.remove(requestId));
  }

  // HÀM ĐÃ SỬA: Gọi hàm async ở trên để tránh block vĩnh viễn
  public Response sendRequestAndWait(Request request) {
    try {
      return sendRequestWithFuture(request, REQUEST_TIMEOUT).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    } catch (ExecutionException e) {
      logger.error("Lỗi gửi request {}: {}", request.getRequestId(), e.getMessage());
      return null;
    }
  }

  public static String uploadFile(String url, byte[] fileBytes) throws Exception {
    String boundary = "boundary123";
    byte[] head = ("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"item.png\"\r\n\r\n")
            .getBytes(StandardCharsets.UTF_8);
    byte[] tail = ("\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\nupload_def\r\n--" + boundary + "--\r\n")
            .getBytes(StandardCharsets.UTF_8);

    byte[] body = new byte[head.length + fileBytes.length + tail.length];
    System.arraycopy(head, 0, body, 0, head.length);
    System.arraycopy(fileBytes, 0, body, head.length, fileBytes.length);
    System.arraycopy(tail, 0, body, head.length + fileBytes.length, tail.length);

    HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build();

    HttpResponse<String> res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
    String responseBody = res.body();
    if (!responseBody.contains("\"secure_url\"")) return null;
    return responseBody.split("\"secure_url\":\"")[1].split("\"")[0];
  }

  private void startHeartbeat() {
    Thread heartbeatThread = new Thread(() -> {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          Thread.sleep(5000);
          if (out != null) {
            sendRequestAsync(new Request(Request.PING, null));
          } else {
            attemptReconnect();
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }, "Heartbeat-Thread");
    heartbeatThread.setDaemon(true);
    heartbeatThread.start();
  }

  private void sendRequestAsync(Request request) {
    try {
      byte[] bytes = jsonMapper.writeValueAsBytes(request);
      synchronized (out) {
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
      }
    } catch (Exception ignored) {}
  }

  // HÀM ĐÃ SỬA: Phục hồi Session khi rớt mạng
  private synchronized void attemptReconnect() {
    try {
      initializeConnection();

      com.auction.shared.User user = com.auction.client.ClientSession.getCurrentUser();
      if (user != null && user.getSessionToken() != null) {
        Request req = new Request(Request.RECONNECT, user.getSessionToken());
        Response res = sendRequestAndWait(req);
        if (res == null || !Response.OK.equals(res.getStatus())) {
          com.auction.client.ui.Main.KhungController.performForcedLogoutFromServer();
        }
      }
    } catch (Exception e) {
      this.out = null;
    }
  }
}