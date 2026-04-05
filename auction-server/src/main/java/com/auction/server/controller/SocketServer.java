package com.auction.server.controller;

import com.auction.server.service.SettlementService; // Nhớ import cái này
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
  private int port;
  private ExecutorService pool;

  public SocketServer(int p) {
    this.port = p;
    this.pool = Executors.newFixedThreadPool(50);
  }

  public void startServer() {
    try {
      // Kích hoạt Robot quét tiền tự động ở đây nè con lợn
      SettlementService ans = new SettlementService();
      ans.start();

      ServerSocket ss = new ServerSocket(this.port);
      System.out.println("server is running");
      while (true) {
        Socket client = ss.accept();
        ClientHandler handler = new ClientHandler(client);
        this.pool.execute(handler);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
