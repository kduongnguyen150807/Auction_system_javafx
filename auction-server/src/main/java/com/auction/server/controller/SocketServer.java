package com.auction.server.controller;

import com.auction.server.dao.user.UserDao;
import com.auction.server.service.auction.AuctionManager;
import com.auction.server.service.auction.SettlementService;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketServer {
  private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);
  private final int port;
  private final ExecutorService pool;

  public SocketServer(int port) {
    this.port = port;
    this.pool = Executors.newFixedThreadPool(50);
  }

  public void startServer() {
    try {
      new UserDao().clearAllSessionTokens();
      AuctionManager.getInstance().clearInMemorySessions();
      SettlementService.getInstance().start();
      ServerSocket serversocket = new ServerSocket(this.port);
      logger.info("server_started_on_port_{}", this.port);
      while (true) {
        Socket client = serversocket.accept();
        ClientHandler handler = new ClientHandler(client);
        this.pool.execute(handler);
      }
    } catch (Exception e) {
      logger.error("socket_server_error", e);
    }
  }
}