package com.auction.server.controller;

import com.auction.server.service.auction.SettlementService;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
  private final int port;
  private final ExecutorService pool;

  public SocketServer(int port) {
    this.port = port;
    this.pool = Executors.newFixedThreadPool(50);
  }

  public void startServer() {
    try {
      SettlementService.getInstance().start();
      ServerSocket serversocket = new ServerSocket(this.port);
      while (true) {
        Socket client = serversocket.accept();
        ClientHandler handler = new ClientHandler(client);
        this.pool.execute(handler);
      }
    } catch (Exception e) {
    }
  }
}