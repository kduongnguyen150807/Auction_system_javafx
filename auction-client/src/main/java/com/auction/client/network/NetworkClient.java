package com.auction.client.network;
import com.auction.shared.*;
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
        } catch (Exception e) { e.printStackTrace(); }
    }
    public static NetworkClient getinstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        NetworkClient ans = instance;
        return ans;
    }
    public void sendrequest(Request req) {
        try {
            this.out.writeObject(req);
            this.out.flush();
        } catch (Exception e) { e.printStackTrace(); }
    }
    public Response receiveresponse() {
        Response ans = null;
        try {
            ans = (Response) this.in.readObject();
        } catch (Exception e) { e.printStackTrace(); }
        return ans;
    }
}