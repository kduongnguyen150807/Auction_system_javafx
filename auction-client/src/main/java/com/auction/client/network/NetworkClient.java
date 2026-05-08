package com.auction.client.network;

import com.auction.shared.Request;
import com.auction.shared.Response;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(NetworkClient.class);
  private static final long DEFAULT_REQUEST_TIMEOUT_SECONDS = 30L;
  private static volatile NetworkClient instance;

  private ObjectOutputStream out;
  private final ConcurrentHashMap<String, CompletableFuture<Response>> pendingMap = new ConcurrentHashMap<>();
  private final List<NetworkEventListener> listeners = new CopyOnWriteArrayList<>();

  private NetworkClient() {
    NetworkConnectionUi ui = new NetworkConnectionUi();
    Optional<String> ipOpt = ui.promptForServerIp();
    if (ipOpt.isEmpty()) {
      LOGGER.error("No server IP provided — all requests will fail until reconnect.");
      return;
    }
    String serverIp = ipOpt.get();
    try {
      LOGGER.info("Connecting to {}:8080 ...", serverIp);
      ObjectSocketConnection conn = ObjectSocketConnection.connect(serverIp, 8080);
      this.out = conn.getOut();
      IncomingResponseRouter router = new IncomingResponseRouter(pendingMap, listeners);
      conn.startReadLoop(
          router::dispatch,
          e -> {
            LOGGER.warn("Server connection lost", e);
            failAllPending(e);
          });
      LOGGER.info("Socket connection established.");
    } catch (Exception e) {
      LOGGER.error("Connection to {}:8080 failed — check server is running.", serverIp, e);
      ui.showConnectionError(serverIp);
    }
  }

  public static NetworkClient getInstance() {
    if (instance == null) synchronized (NetworkClient.class) { if (instance == null) instance = new NetworkClient(); }
    return instance;
  }

  public void addListener(NetworkEventListener l) { if (!listeners.contains(l)) listeners.add(l); }
  public void removeListener(NetworkEventListener l) { listeners.remove(l); }

  private void failAllPending(Throwable cause) {
    pendingMap.forEach((id, f) -> f.completeExceptionally(cause));
    pendingMap.clear();
  }

  private CompletableFuture<Response> sendRequestAsync(Request request, long timeoutSeconds) {
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
      return sendRequestAsync(request, DEFAULT_REQUEST_TIMEOUT_SECONDS).get();
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
