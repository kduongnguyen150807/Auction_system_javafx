package com.auction.client.network;

import com.auction.shared.ChatMessage;
import com.auction.shared.Friendship;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(NetworkClient.class);
  private static final long DEFAULT_REQUEST_TIMEOUT_SECONDS = 30L;
  private static volatile NetworkClient instance;

  private Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;
  private final ConcurrentHashMap<String, CompletableFuture<Response>> pendingMap = new ConcurrentHashMap<>();
  private final List<NetworkEventListener> listeners = new CopyOnWriteArrayList<>();

  private NetworkClient() {
    String serverIp = promptForServerIp();
    if (serverIp == null || serverIp.isBlank()) {
      LOGGER.error("No server IP provided — all requests will fail until reconnect.");
      return;
    }
    try {
      LOGGER.info("Connecting to {}:8080 ...", serverIp);
      this.socket = new Socket(serverIp, 8080);
      this.out = new ObjectOutputStream(this.socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.socket.getInputStream());
      LOGGER.info("Socket connection established.");
      startListener();
    } catch (Exception e) {
      LOGGER.error("Connection to {}:8080 failed — check server is running.", serverIp, e);
      showConnectionError(serverIp);
    }
  }

  private void showConnectionError(String ip) {
    Runnable alert = () -> {
      javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
      a.setTitle("Connection Failed");
      a.setHeaderText("Cannot reach server at " + ip + ":8080");
      a.setContentText("Make sure the server is running and the IP address is correct,\nthen restart the application.");
      a.showAndWait();
    };
    if (Platform.isFxApplicationThread()) alert.run(); else Platform.runLater(alert);
  }

  public static NetworkClient getInstance() {
    if (instance == null) synchronized (NetworkClient.class) { if (instance == null) instance = new NetworkClient(); }
    return instance;
  }

  public void addListener(NetworkEventListener l) { if (!listeners.contains(l)) listeners.add(l); }
  public void removeListener(NetworkEventListener l) { listeners.remove(l); }

  private String promptForServerIp() {
    while (true) {
      javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("127.0.0.1");
      dialog.setTitle("Server IP");
      dialog.setHeaderText("Enter Server IP address:");
      dialog.setContentText("IP (e.g. 127.0.0.1 or 192.168.1.x):");
      java.util.Optional<String> result = dialog.showAndWait();
      if (result.isEmpty()) return null;
      String ip = result.get().trim();
      if (!ip.isBlank()) return ip;
      new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING,
          "IP address cannot be empty.").showAndWait();
    }
  }

  private void startListener() {
    Thread t = new Thread(() -> {
      try {
        while (true) {
          Object obj = in.readObject();
          if (obj instanceof Response response) handleIncoming(response);
        }
      } catch (Exception e) {
        LOGGER.warn("Server connection lost", e);
        failAllPending(e);
      }
    }, "NetworkClient-Listener");
    t.setDaemon(true);
    t.start();
  }

  private void failAllPending(Throwable cause) { pendingMap.forEach((id, f) -> f.completeExceptionally(cause)); pendingMap.clear(); }
  private void forListeners(Consumer<NetworkEventListener> action) { Platform.runLater(() -> listeners.forEach(action)); }

  private void handleIncoming(Response response) {
    switch (response.getStatus()) {
      case "BALANCE_UPDATE" -> {
        if (response.getPayload() instanceof User u) forListeners(l -> l.onBalanceUpdate(u));
        return;
      }
      case "OUTBID_NOTIFY" -> {
        int itemId = response.getPayload() instanceof Integer i ? i : -1;
        forListeners(l -> l.onOutbidNotify(itemId));
        return;
      }
      case "CHAT_GLOBAL" -> {
        if (response.getPayload() instanceof ChatMessage m) forListeners(l -> l.onGlobalChat(m));
        return;
      }
      case "CHAT_PRIVATE" -> {
        if (response.getPayload() instanceof ChatMessage m) forListeners(l -> l.onPrivateChat(m));
        return;
      }
      case "FRIEND_REQUEST" -> {
        if (response.getPayload() instanceof Friendship f) forListeners(l -> l.onFriendRequest(f));
        return;
      }
      case "FRIEND_REQUEST_SENT" -> {
        if (response.getPayload() instanceof Friendship f) forListeners(l -> l.onFriendRequestSent(f));
        return;
      }
      case "FRIEND_ACCEPTED" -> {
        if (response.getPayload() instanceof Friendship f) forListeners(l -> l.onFriendAccepted(f));
        return;
      }
      case "SELLER_BID_NOTIFY" -> {
        if (response.getPayload() instanceof Item item)
          forListeners(l -> l.onSellerBidNotify(item, item.getCurrentPrice()));
        return;
      }
      case "ITEM_CLOSED" -> {
        if (response.getPayload() instanceof Item item) forListeners(l -> l.onItemClosed(item));
        return;
      }
      case "NEW_BID_UPDATE" -> {
        if (response.getPayload() instanceof Item item) forListeners(l -> l.onNewBidUpdate(item));
        return;
      }
      case Response.ACCOUNT_BANNED -> { forListeners(l -> l.onAccountBanned(response.getMessage())); return; }
      case Response.ACCOUNT_UNBANNED -> { forListeners(NetworkEventListener::onAccountUnbanned); return; }
      default -> {}
    }
    if ("priceupdate".equals(response.getMessage()) && response.getPayload() instanceof Item item) {
      forListeners(l -> l.onNewBidUpdate(item)); return;
    }
    String requestId = response.getRequestId();
    if (requestId == null) return;
    CompletableFuture<Response> future = pendingMap.get(requestId);
    if (future != null) future.complete(response);
    else LOGGER.warn("Received response for unknown request ID: {}", requestId);
  }

  public CompletableFuture<Response> sendRequestAsync(Request request) {
    return sendRequestAsync(request, DEFAULT_REQUEST_TIMEOUT_SECONDS);
  }

  public CompletableFuture<Response> sendRequestAsync(Request request, long timeoutSeconds) {
    String requestId = request.getRequestId();
    CompletableFuture<Response> future = new CompletableFuture<>();
    if (out == null) {
      future.completeExceptionally(new IllegalStateException(
          "Not connected to server — check IP address and server status."));
      return future;
    }
    CompletableFuture<Response> existing = pendingMap.putIfAbsent(requestId, future);
    if (existing != null) {
      future.completeExceptionally(new IllegalStateException("Duplicate request id: " + requestId));
      return future;
    }
    try {
      synchronized (out) { out.writeObject(request); out.flush(); }
    } catch (IOException e) {
      pendingMap.remove(requestId);
      future.completeExceptionally(e);
    }
    return future.orTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .whenComplete((response, error) -> {
          pendingMap.remove(requestId);
          if (error != null) {
            if (error instanceof java.util.concurrent.TimeoutException)
              LOGGER.warn("Server timeout ({}s) for request {} ({})", timeoutSeconds, requestId, request.getAction());
            else
              LOGGER.warn("Request {} ({}) failed: {}", requestId, request.getAction(), error.getMessage());
          }
        });
  }

  public Response sendRequestAndWait(Request request) {
    try {
      return sendRequestAsync(request).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOGGER.warn("Interrupted while waiting for response to {}", request.getAction());
      return null;
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof TimeoutException) return null;
      LOGGER.error("Socket send/receive error", cause);
      return null;
    }
  }

  public static String uploadFile(String urlString, byte[] fileBytes) throws Exception {
    String boundary = "boundary" + System.currentTimeMillis();
    java.net.URL url = java.net.URI.create(urlString).toURL();
    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    try (OutputStream outStream = conn.getOutputStream()) {
      outStream.write(("--" + boundary + "\r\n").getBytes());
      outStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"avatar.png\"\r\n\r\n").getBytes());
      outStream.write(fileBytes);
      outStream.write(("\r\n--" + boundary + "\r\n").getBytes());
      outStream.write(("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n").getBytes());
      outStream.write(("upload_def\r\n").getBytes());
      outStream.write(("--" + boundary + "--\r\n").getBytes());
    }
    try (java.util.Scanner scanner = new java.util.Scanner(conn.getInputStream())) {
      String body = scanner.useDelimiter("\\A").next();
      com.fasterxml.jackson.databind.JsonNode node =
          new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
      return node.get("secure_url").asText();
    }
  }
}
