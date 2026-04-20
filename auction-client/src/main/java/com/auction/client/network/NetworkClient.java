package com.auction.client.network;

import com.auction.shared.ChatMessage;
import com.auction.shared.Friendship;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

public class NetworkClient {
  private static final Logger LOGGER = Logger.getLogger(NetworkClient.class.getName());

  private static NetworkClient instance;
  private Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;
  private final ConcurrentHashMap<String, LinkedBlockingQueue<Response>> pendingMap =
      new ConcurrentHashMap<>();
  private final List<NetworkEventListener> listeners = new CopyOnWriteArrayList<>();

  private NetworkClient() {
    try {
      String serverIp = "127.0.0.1";
      if (Platform.isFxApplicationThread()) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("127.0.0.1");
        dialog.setTitle("IP Setup");
        dialog.setHeaderText("Nhập IP Server:");
        serverIp = dialog.showAndWait().orElse("127.0.0.1");
      } else {
        java.util.concurrent.FutureTask<String> task = new java.util.concurrent.FutureTask<>(() -> {
          javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("127.0.0.1");
          dialog.setTitle("IP Setup");
          dialog.setHeaderText("Nhập IP Server:");
          return dialog.showAndWait().orElse("127.0.0.1");
        });
        Platform.runLater(task);
        serverIp = task.get();
      }

      LOGGER.info("Connecting to: " + serverIp + ":8080...");
      this.socket = new Socket(serverIp, 8080);
      this.out = new ObjectOutputStream(this.socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.socket.getInputStream());
      LOGGER.info("Socket connection established.");
      startListener();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Initial connection failed", e);
    }
  }

  public static synchronized NetworkClient getInstance() {
    if (instance == null) instance = new NetworkClient();
    return instance;
  }

  public void addListener(NetworkEventListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(NetworkEventListener listener) {
    this.listeners.remove(listener);
  }

  private void startListener() {
    Thread listenerThread = new Thread(() -> {
      try {
        while (true) {
          Object obj = in.readObject();
          if (obj instanceof Response) {
            handleIncoming((Response) obj);
          }
        }
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Server connection lost", e);
      }
    });
    listenerThread.setDaemon(true);
    listenerThread.start();
  }

  private void handleIncoming(Response response) {
    if ("BALANCE_UPDATE".equals(response.getStatus())) {
      User user = (User) response.getPayload();
      Platform.runLater(() -> {
        for (NetworkEventListener listener : listeners) {
          listener.onBalanceUpdate(user);
        }
      });
      return;
    }

    if ("OUTBID_NOTIFY".equals(response.getStatus())) {
      Object payload = response.getPayload();
      int itemId = payload instanceof Integer ? (int) payload : -1;
      Platform.runLater(() -> {
        for (NetworkEventListener listener : listeners) {
          listener.onOutbidNotify(itemId);
        }
      });
      return;
    }

    if ("CHAT_GLOBAL".equals(response.getStatus())) {
      Object payload = response.getPayload();
      if (payload instanceof ChatMessage msg) {
        Platform.runLater(() -> {
          for (NetworkEventListener listener : listeners) {
            listener.onGlobalChat(msg);
          }
        });
      }
      return;
    }

    if ("CHAT_PRIVATE".equals(response.getStatus())) {
      Object payload = response.getPayload();
      if (payload instanceof ChatMessage msg) {
        Platform.runLater(() -> {
          for (NetworkEventListener listener : listeners) {
            listener.onPrivateChat(msg);
          }
        });
      }
      return;
    }

    if ("FRIEND_REQUEST".equals(response.getStatus())) {
      Object payload = response.getPayload();
      if (payload instanceof Friendship f) {
        Platform.runLater(() -> {
          for (NetworkEventListener listener : listeners) listener.onFriendRequest(f);
        });
      }
      return;
    }

    if ("FRIEND_REQUEST_SENT".equals(response.getStatus())) {
      Object payload = response.getPayload();
      if (payload instanceof Friendship f) {
        Platform.runLater(() -> {
          for (NetworkEventListener listener : listeners) listener.onFriendRequestSent(f);
        });
      }
      return;
    }

    if ("FRIEND_ACCEPTED".equals(response.getStatus())) {
      Object payload = response.getPayload();
      if (payload instanceof Friendship f) {
        Platform.runLater(() -> {
          for (NetworkEventListener listener : listeners) listener.onFriendAccepted(f);
        });
      }
      return;
    }

    if ("SELLER_BID_NOTIFY".equals(response.getStatus())) {
      Object payload = response.getPayload();
      if (payload instanceof Item item) {
        double newPrice = item.getCurrentPrice();
        Platform.runLater(() -> {
          for (NetworkEventListener listener : listeners) listener.onSellerBidNotify(item, newPrice);
        });
      }
      return;
    }

    if ("ITEM_CLOSED".equals(response.getStatus())) {
      Object payload = response.getPayload();
      if (payload instanceof Item item) {
        Platform.runLater(() -> {
          for (NetworkEventListener listener : listeners) {
            listener.onItemClosed(item);
          }
        });
      }
      return;
    }

    if ("NEW_BID_UPDATE".equals(response.getStatus())
        || ("priceupdate".equals(response.getMessage()) && response.getPayload() instanceof Item)) {
      Object payload = response.getPayload();
      if (payload instanceof Item item) {
        Platform.runLater(() -> {
          for (NetworkEventListener listener : listeners) {
            listener.onNewBidUpdate(item);
          }
        });
      }
      return;
    }

    String requestId = response.getRequestId();
    if (requestId != null) {
      LinkedBlockingQueue<Response> queue = pendingMap.get(requestId);
      if (queue != null) {
        queue.offer(response);
      } else {
        LOGGER.warning("Received response for unknown request ID: " + requestId);
      }
    }
  }

  public Response sendRequestAndWait(Request request) {
    try {
      LinkedBlockingQueue<Response> queue = new LinkedBlockingQueue<>();
      pendingMap.put(request.getRequestId(), queue);
      synchronized (out) {
        out.writeObject(request);
        out.flush();
      }

      Response response = queue.poll(30, TimeUnit.SECONDS);
      if (response == null) {
        LOGGER.warning("Server timeout (30s) for request: " + request.getRequestId());
      }
      pendingMap.remove(request.getRequestId());
      return response;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Socket send/receive error", e);
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
      String responseBody = scanner.useDelimiter("\\A").next();
      com.fasterxml.jackson.databind.JsonNode node =
          new com.fasterxml.jackson.databind.ObjectMapper().readTree(responseBody);
      return node.get("secure_url").asText();
    }
  }
}
