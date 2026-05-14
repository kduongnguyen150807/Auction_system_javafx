package com.auction.server;

import com.auction.server.controller.SocketServer;
import com.auction.server.dao.transaction.TransactionManager;
import com.auction.server.dao.daoMigration.DatabaseMigration;

public class Main {
  public static void main(String[] args) {
    TransactionManager.execute(conn -> DatabaseMigration.runAll(conn));
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.init();

    SocketServer server = new SocketServer(8080, bootstrap.getRequestDispatcher());
    server.start();
  }
}
