package com.auction.server;

import com.auction.server.controller.SocketServer;

public class Main {
  public static void main(String[] args) {
    SocketServer server = new SocketServer(8080);
    server.startServer();
  }
}
