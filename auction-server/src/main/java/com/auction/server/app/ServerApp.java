package com.auction.server.app;
import java.net.ServerSocket;
import java.net.Socket;
import com.auction.server.network.ClientHandler;
public class ServerApp {
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(8888);
            System.out.println("--- auction server dang chay tai cong 8888 ---");
            while (true) {
                Socket s = ss.accept();
                System.out.println("co thang moi vao: " + s.getInetAddress());
                ClientHandler ch = new ClientHandler(s);
                Thread t = new Thread(ch);
                t.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}