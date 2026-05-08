package com.auction.server;

import com.auction.server.controller.SocketServer;
import com.auction.server.dao.platform.DatabaseMigration;

public class Main {
  public static void main(String[] args) {
    DatabaseMigration.runAll();
    SocketServer server = new SocketServer(8080);
    server.startServer();
  }
}
