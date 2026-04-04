package com.auction.client.network;

import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import com.auction.client.ClientSession;
import com.auction.client.ui.Profile.ProfileController;
import com.auction.client.ui.Main.KhungController;
import com.auction.client.ui.TrangChu.TrangChuController;
import com.auction.client.util.NotificationCenter;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NetworkClient {
    private static NetworkClient instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final ConcurrentHashMap<String, LinkedBlockingQueue<Response>> pendingmap = new ConcurrentHashMap<>();

    private NetworkClient() {
        try {
            this.socket = new Socket("localhost", 8080);
            this.out = new ObjectOutputStream(this.socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(this.socket.getInputStream());
            startlistener();
        } catch (Exception e) {}
    }

    public static synchronized NetworkClient getinstance() {
        if (instance == null) instance = new NetworkClient();
        return instance;
    }

    private void startlistener() {
        Thread res = new Thread(() -> {
            try {
                while (true) {
                    Object ans = in.readObject();
                    if (ans instanceof Response) {
                        try {
                            handleincoming((Response) ans);
                        } catch (Exception e) {}
                    }
                }
            } catch (Exception e) {}
        });
        res.setDaemon(true);
        res.start();
    }

    private void handleincoming(Response res) {
        if ("BALANCE_UPDATE".equals(res.getstatus())) {
            User res1 = (User) res.getpayload();
            Platform.runLater(() -> {
                if (ProfileController.getinstance() != null) {
                    ProfileController.getinstance().updatebalancedirectly(res1);
                } else {
                    ClientSession.setCurrentUser(res1);
                }
            });
            return;
        }
        if ("OUTBID_NOTIFY".equals(res.getstatus())) {
            int res2 = (int) res.getpayload();
            NotificationCenter.addnotification("\uD83D\uDD25 B\u00C1O \u0110\u1ED8NG: S\u1EA3n ph\u1EA9m m\u00E3 " + res2 + " v\u1EEBa b\u1ECB ng\u01B0\u1EDDi kh\u00E1c tr\u1EA3 gi\u00E1 cao h\u01A1n! H\u00FAp l\u1EA1i ngay!");
            return;
        }
        if ("PRICE_UPDATE".equals(res.getstatus())) {
            Object res3 = res.getpayload();
            if (res3 instanceof Item) {
                Item res4 = (Item) res3;
                Platform.runLater(() -> {
                    if (KhungController.infoc != null && KhungController.infoc.getid() == res4.getid()) {
                        KhungController.infoc.updatepriceui(res4);
                    }
                    TrangChuController ans2 = TrangChuController.getinstance();
                    if (ans2 != null) {
                        ans2.updateitemprice(res4);
                    }
                });
            }
            return;
        }
        String res5 = res.getrequestid();
        if (res5 != null) {
            LinkedBlockingQueue<Response> res6 = pendingmap.get(res5);
            if (res6 != null) {
                res6.offer(res);
            }
        }
    }

    public Response sendrequestandwait(Request req) {
        Response ans = null;
        try {
            LinkedBlockingQueue<Response> res = new LinkedBlockingQueue<>();
            pendingmap.put(req.getrequestid(), res);
            synchronized (out) {
                out.writeObject(req);
                out.flush();
            }
            ans = res.poll(30, TimeUnit.SECONDS);
            pendingmap.remove(req.getrequestid());
        } catch (Exception e) {}
        return ans;
    }

    public static String uploadfile(String urlString, byte[] fileBytes) throws Exception {
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