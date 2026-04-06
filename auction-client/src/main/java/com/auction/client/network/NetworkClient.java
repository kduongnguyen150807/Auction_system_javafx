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
        javafx.scene.control.TextInputDialog ans = new javafx.scene.control.TextInputDialog("127.0.0.1");
        ans.setTitle("IP Setup");
        ans.setHeaderText("Nhập IP Server đi mày:");
        ans1 = ans.showAndWait().orElse("127.0.0.1");
      } else {
        java.util.concurrent.FutureTask<String> res = new java.util.concurrent.FutureTask<>(() -> {
          javafx.scene.control.TextInputDialog ans = new javafx.scene.control.TextInputDialog("127.0.0.1");
          ans.setTitle("IP Setup");
          ans.setHeaderText("Nhập IP Server đi mày:");
          return ans.showAndWait().orElse("127.0.0.1");
        });
        Platform.runLater(res);
        ans1 = res.get();
      }

      System.out.println("Đang thử kết nối tới: " + ans1 + ":8080...");
      this.socket = new Socket(ans1, 8080);
      this.out = new ObjectOutputStream(this.socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.socket.getInputStream());
      System.out.println("Kết nối Socket THÀNH CÔNG!");
      startListener();
    } catch (Exception e) {
      System.err.println("LỖI KẾT NỐI BAN ĐẦU:");
      e.printStackTrace();
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
                          System.out.println("Client nhận được object: " + ans.getClass().getSimpleName());
                          if (ans instanceof Response) {
                            handleIncoming((Response) ans);
                          }
                        }
                      } catch (Exception e) {
                        System.err.println("Mất kết nối với Server (Listener dừng):");
                        e.printStackTrace();
                      }
                    });
    res.setDaemon(true);
    res.start();
  }

  private void handleIncoming(Response res) {
    System.out.println("Đang xử lý Response. ID: " + res.getRequestId() + " | Status: " + res.getStatus());

    if ("BALANCE_UPDATE".equals(res.getStatus())) {
      User res1 = (User) res.getPayload();
      Platform.runLater(() -> {
        if (ProfileController.getInstance() != null) ProfileController.getInstance().updateBalanceDirectly(res1);
        else ClientSession.setCurrentUser(res1);
      });
      return;
    }

    if ("OUTBID_NOTIFY".equals(res.getStatus())) {
      Object payload = res.getPayload();
      String res2 = payload != null ? payload.toString() : "N/A";
      NotificationCenter.addNotification("🔥 BÁO ĐỘNG: Sản phẩm mã " + res2 + " bị đè giá rồi!");
      return;
    }

    if ("NEW_BID_UPDATE".equals(res.getStatus())
        || ("priceupdate".equals(res.getMessage()) && res.getPayload() instanceof Item)) {
      Object res3 = res.getPayload();
      if (res3 instanceof Item i) {
        KhungController.updateRealtimeUi(i);
      }
      return;
    }

    String res5 = res.getRequestId();
    if (res5 != null) {
      LinkedBlockingQueue<Response> res6 = pendingMap.get(res5);
      if (res6 != null) {
        res6.offer(res);
        System.out.println("Đã đẩy Response vào queue cho RequestID: " + res5);
      } else {
        System.err.println("CẢNH BÁO: Nhận được Response nhưng không tìm thấy queue nào đợi ID: " + res5);
      }
    }
  }

  public Response sendRequestAndWait(Request req) {
    Response ans = null;
    System.out.println("Gửi Request ID: " + req.getRequestId());
    try {
      LinkedBlockingQueue<Response> res = new LinkedBlockingQueue<>();
      pendingMap.put(req.getRequestId(), res);
      synchronized (out) {
        out.writeObject(req);
        out.flush();
      }

      ans = res.poll(30, TimeUnit.SECONDS);

      if (ans == null) {
        System.err.println("LỖI: Server không phản hồi (Timeout 30s) ID: " + req.getRequestId());
      } else {
        System.out.println("Đã nhận phản hồi thành công!");
      }

      pendingMap.remove(req.getRequestId());
    } catch (Exception e) {
      System.err.println("LỖI SOCKET KHI GỬI/NHẬN:");
      e.printStackTrace();
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
      out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"avatar.png\"\r\n\r\n").getBytes());
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