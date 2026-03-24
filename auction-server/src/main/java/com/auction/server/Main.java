package com.auction.server;
import com.auction.server.dao.DatabaseConnection;
import com.auction.server.controller.SocketServer;
public class Main {
    public static void main(String[] args) {
        DatabaseConnection dbcon = DatabaseConnection.getinstance();
        int port = 8080;
        SocketServer server = new SocketServer(port);
        server.startserver();
    }
}