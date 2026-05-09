package com.auction.client.network;

import com.auction.shared.Request;
import com.auction.shared.Response;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class NetworkClient {
  private static final long defaultrequesttimeoutseconds = 30L;
  private static volatile NetworkClient instance;

  private ObjectOutputStream out;
  private final ConcurrentHashMap<String, CompletableFuture<Response>> pendingmap = new ConcurrentHashMap<>();
  private final List<NetworkEventListener> listeners = new CopyOnWriteArrayList<>();
  private String serverip;

  private NetworkClient() {
    NetworkConnectionUi ui = new NetworkConnectionUi();
    Optional<String> ipopt = ui.promptForServerIp();
    if (ipopt.isEmpty()) {
      return;
    }
    serverip = ipopt.get();
    try {
      ObjectSocketConnection conn = ObjectSocketConnection.connect(serverip, 8080);
      this.out = conn.getOut();
      IncomingResponseRouter router = new IncomingResponseRouter(pendingmap, listeners);
      conn.startReadLoop(
              router::dispatch,
              e -> {
                this.out = null;
                failallpending(e);
              });
      startheartbeat();
    } catch (Exception e) {
      ui.showConnectionError(serverip);
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
    NetworkClient ans = instance;
    return ans;
  }

  public void addListener(NetworkEventListener l) {
    if (!listeners.contains(l)) {
      listeners.add(l);
    }
  }

  public void removeListener(NetworkEventListener l) {
    listeners.remove(l);
  }

  private void failallpending(Throwable cause) {
    pendingmap.forEach((id, f) -> f.completeExceptionally(cause));
    pendingmap.clear();
  }

  private CompletableFuture<Response> sendrequestasync(Request request, long timeoutseconds) {
    String requestid = request.getRequestId();
    CompletableFuture<Response> ans = new CompletableFuture<>();
    if (out == null) {
      ans.completeExceptionally(new IllegalStateException("not connected"));
      return ans;
    }
    CompletableFuture<Response> existing = pendingmap.putIfAbsent(requestid, ans);
    if (existing != null) {
      ans.completeExceptionally(new IllegalStateException("duplicate"));
      return ans;
    }
    try {
      synchronized (out) {
        out.reset();
        out.writeObject(request);
        out.flush();
      }
    } catch (IOException e) {
      pendingmap.remove(requestid);
      ans.completeExceptionally(e);
    }
    CompletableFuture<Response> res =
            ans.orTimeout(timeoutseconds, TimeUnit.SECONDS)
                    .whenComplete(
                            (r, error) -> {
                              pendingmap.remove(requestid);
                            });
    return res;
  }

  public Response sendRequestAndWait(Request request) {
    try {
      Response ans = sendrequestasync(request, defaultrequesttimeoutseconds).get();
      return ans;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    } catch (ExecutionException e) {
      return null;
    }
  }

  public static String uploadFile(String urlstring, byte[] filebytes) throws Exception {
    String boundary = "boundary123";
    byte[] head =
            ("--"
                    + boundary
                    + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"item.png\"\r\n\r\n")
                    .getBytes();
    byte[] tail =
            ("\r\n--"
                    + boundary
                    + "\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\nupload_def\r\n--"
                    + boundary
                    + "--\r\n")
                    .getBytes();
    byte[] body = new byte[head.length + filebytes.length + tail.length];
    System.arraycopy(head, 0, body, 0, head.length);
    System.arraycopy(filebytes, 0, body, head.length, filebytes.length);
    System.arraycopy(tail, 0, body, head.length + filebytes.length, tail.length);
    HttpRequest req =
            HttpRequest.newBuilder()
                    .uri(URI.create(urlstring))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();
    HttpResponse<String> res =
            HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
    String responsebody = res.body();
    if (!responsebody.contains("\"secure_url\"")) {
      return null;
    }
    String ans = responsebody.split("\"secure_url\":\"")[1].split("\"")[0];
    return ans;
  }

  private void startheartbeat() {
    Thread thread =
            new Thread(
                    () -> {
                      while (true) {
                        try {
                          Thread.sleep(5000);
                          if (out != null) {
                            Request req = new Request(Request.PING, null);
                            sendrequestasync(req, 2);
                          } else {
                            reconnect();
                          }
                        } catch (InterruptedException e) {
                          Thread.currentThread().interrupt();
                          break;
                        }
                      }
                    });
    thread.setDaemon(true);
    thread.start();
  }

  private synchronized void reconnect() {
    try {
      ObjectSocketConnection conn = ObjectSocketConnection.connect(serverip, 8080);
      this.out = conn.getOut();
      IncomingResponseRouter router = new IncomingResponseRouter(pendingmap, listeners);
      conn.startReadLoop(
              router::dispatch,
              e -> {
                this.out = null;
                failallpending(e);
              });
      com.auction.shared.User user = com.auction.client.ClientSession.getCurrentUser();
      if (user != null && user.getSessiontoken() != null) {
        Request req = new Request(Request.RECONNECT, user.getSessiontoken());
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