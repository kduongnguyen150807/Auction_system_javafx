package com.auction.server;
import com.auction.server.controller.SocketServer;
import com.auction.server.service.AuctionCloser;
public class Main {
    public static void main(String[] args) {
        new AuctionCloser().start();
        SocketServer server = new SocketServer(8080);
        server.startserver();
    }
}