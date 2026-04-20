package com.auction.server.controller;

import com.auction.server.service.SettlementService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketServer {
  private static final Logger LOGGER = Logger.getLogger(SocketServer.class.getName());

  private final int port;
  private final ExecutorService pool;

  public SocketServer(int port) {
    this.port = port;
    this.pool = Executors.newFixedThreadPool(50);
  }

  public void startServer() {
    try {
      SettlementService settlement = new SettlementService();
      settlement.start();

      ServerSocket serverSocket = new ServerSocket(this.port);
      LOGGER.info("Server is running on port " + this.port);
      while (true) {
        Socket client = serverSocket.accept();
        ClientHandler handler = new ClientHandler(client);
        this.pool.execute(handler);
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Server startup failed", e);
    }
  }
}
