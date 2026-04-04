package com.auction.client.network;

import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import com.auction.client.ClientSession;
import com.auction.client.ui.Profile.ProfileController;
import java.io.*;
import java.net.Socket;

public class NetworkClient {
    private static NetworkClient instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private NetworkClient() {
        try {
            this.socket = new Socket("localhost", 8080);
            this.out = new ObjectOutputStream(this.socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(this.socket.getInputStream());
        } catch (Exception e) {}
    }

    public static synchronized NetworkClient getinstance() {
        if (instance == null) instance = new NetworkClient();
        return instance;
    }

    public synchronized Response sendrequestandwait(Request req) {
        Response ans = null;
        try {
            this.out.writeObject(req);
            this.out.flush();
            Object res = this.in.readObject();
            if (res instanceof Response) {
                ans = (Response) res;
                if ("BALANCE_UPDATE".equals(ans.getstatus())) {
                    User u = (User) ans.getpayload();
                    if (ProfileController.getinstance() != null) {
                        ProfileController.getinstance().updatebalancedirectly(u);
                    } else {
                        ClientSession.setCurrentUser(u);
                    }
                    ans = (Response) this.in.readObject();
                }
            }
        } catch (Exception e) {}
        return ans;
    }

    public static String uploadfile(String urlString, byte[] fileBytes) throws Exception {
        String res = "boundary" + System.currentTimeMillis();
        java.net.URL url = new java.net.URL(urlString);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
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