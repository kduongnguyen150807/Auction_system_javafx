package com.auction.client.network;

import com.auction.client.ClientSession;
import com.auction.client.ui.Main.KhungController;
import com.auction.client.ui.Profile.ProfileController;
import com.auction.client.ui.TrangChu.TrangChuController;
import com.auction.client.util.NotificationCenter;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

public class NetworkClient {
  private static NetworkClient instance;
  private Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;
  private final ConcurrentHashMap<String, LinkedBlockingQueue<Response>> pendingMap =
      new ConcurrentHashMap<>();

  private NetworkClient() {
    try {
      String ans1 = "127.0.0.1";
      if (Platform.isFxApplicationThread()) {
        javafx.scene.control.TextInputDialog ans = new javafx.scene.control.TextInputDialog("10.11.");
        ans.setTitle("IP");
        ans.setHeaderText("IP:");
        ans1 = ans.showAndWait().orElse("127.0.0.1");
      } else {
        java.util.concurrent.FutureTask<String> res = new java.util.concurrent.FutureTask<>(() -> {
          javafx.scene.control.TextInputDialog ans = new javafx.scene.control.TextInputDialog("10.11.");
          ans.setTitle("IP");
          ans.setHeaderText("IP:");
          return ans.showAndWait().orElse("127.0.0.1");
        });
        Platform.runLater(res);
        ans1 = res.get();
      }

      this.socket = new Socket(ans1, 8080);
      this.out = new ObjectOutputStream(this.socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.socket.getInputStream());
      startListener();
    } catch (Exception e) {
    }
  }

  public static synchronized NetworkClient getInstance() {
    if (instance == null) instance = new NetworkClient();
    return instance;
  }

  private void startListener() {
    Thread res =
        new Thread(
            () -> {
              try {
                while (true) {
                  Object ans = in.readObject();
                  if (ans instanceof Response) {
                    try {
                      handleIncoming((Response) ans);
                    } catch (Exception e) {
                    }
                  }
                }
              } catch (Exception e) {
              }
            });
    res.setDaemon(true);
    res.start();
  }

  private void handleIncoming(Response res) {
    if ("BALANCE_UPDATE".equals(res.getStatus())) {
      User res1 = (User) res.getPayload();
      Platform.runLater(
          () -> {
            if (ProfileController.getInstance() != null) {
              ProfileController.getInstance().updateBalanceDirectly(res1);
            } else {
              ClientSession.setCurrentUser(res1);
            }
          });
      return;
    }
    if ("OUTBID_NOTIFY".equals(res.getStatus())) {
      int res2 = (int) res.getPayload();
      NotificationCenter.addNotification(
          "\uD83D\uDD25 B\u00C1O \u0110\u1ED8NG: S\u1EA3n ph\u1EA9m m\u00E3 "
              + res2
              + " v\u1EEBa b\u1ECB ng\u01B0\u1EDDi kh\u00E1c tr\u1EA3 gi\u00E1 cao h\u01A1n! H\u00FAp l\u1EA1i ngay!");
      return;
    }
    if ("PRICE_UPDATE".equals(res.getStatus())) {
      Object res3 = res.getPayload();
      if (res3 instanceof Item) {
        Item res4 = (Item) res3;
        Platform.runLater(
            () -> {
              if (KhungController.itemDetailController != null
                  && KhungController.itemDetailController.getId() == res4.getId()) {
                KhungController.itemDetailController.updatePriceUi(res4);
              }
              TrangChuController ans2 = TrangChuController.getInstance();
              if (ans2 != null) {
                ans2.updateItemPrice(res4);
              }
            });
      }
      return;
    }
    String res5 = res.getRequestId();
    if (res5 != null) {
      LinkedBlockingQueue<Response> res6 = pendingMap.get(res5);
      if (res6 != null) {
        res6.offer(res);
      }
    }
  }

  public Response sendRequestAndWait(Request req) {
    Response ans = null;
    try {
      LinkedBlockingQueue<Response> res = new LinkedBlockingQueue<>();
      pendingMap.put(req.getRequestId(), res);
      synchronized (out) {
        out.writeObject(req);
        out.flush();
      }
      ans = res.poll(30, TimeUnit.SECONDS);
      pendingMap.remove(req.getRequestId());
    } catch (Exception e) {
    }
    return ans;
  }

  public static String uploadFile(String urlString, byte[] fileBytes) throws Exception {
    String res = "boundary" + System.currentTimeMillis();
    java.net.URL ans1 = java.net.URI.create(urlString).toURL();
    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) ans1.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + res);
    try (OutputStream out = conn.getOutputStream()) {
      out.write(("--" + res + "\r\n").getBytes());
      out.write(
          ("Content-Disposition: form-data; name=\"file\"; filename=\"avatar.png\"\r\n\r\n")
              .getBytes());
      out.write(fileBytes);
      out.write(("\r\n--" + res + "\r\n").getBytes());
      out.write(("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n").getBytes());
      out.write(("upload_def\r\n").getBytes());
      out.write(("--" + res + "--\r\n").getBytes());
    }
    try (java.util.Scanner s = new java.util.Scanner(conn.getInputStream())) {
      String ans = s.useDelimiter("\\A").next();
      return ans.split("\"secure_url\":\"")[1].split("\"")[0];
    }
  }
}
