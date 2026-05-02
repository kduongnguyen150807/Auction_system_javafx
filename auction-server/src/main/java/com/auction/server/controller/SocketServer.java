package com.auction.server.controller;

import com.auction.server.dao.DatabaseInitializer;
import com.auction.server.service.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {
  private final int port;
  private final ExecutorService pool;
  private final RequestDispatcher dispatcher;

  public SocketServer(int port) {
    this.port = port;
    this.pool = Executors.newFixedThreadPool(50);

    UserService userService = new UserService();
    WalletService walletService = new WalletService();
    TransactionService transactionService = new TransactionService();
    ItemService itemService = new ItemService();
    ItemQueryService itemQueryService = new ItemQueryService();
    LotService lotService = new LotService();
    BidQueryService bidQueryService = new BidQueryService();
    RatingService ratingService = new RatingService();
    AdminService adminService = new AdminService();

    this.dispatcher =
            new RequestDispatcher(
                    List.of(
                            new AuthRequestHandler(userService),
                            new UserRequestHandler(userService, walletService, transactionService),
                            new ItemRequestHandler(itemService, itemQueryService, lotService),
                            new BidRequestHandler(AuctionManager.getInstance(), bidQueryService),
                            new RatingRequestHandler(ratingService),
                            new AdminRequestHandler(adminService)));
  }

  public void startServer() {
    try {
      DatabaseInitializer.init();

      SettlementService settlementService = new SettlementService();
      settlementService.start();

      ServerSocket ss = new ServerSocket(this.port);
      System.out.println("server is running");

      while (true) {
        Socket client = ss.accept();
        ClientHandler handler = new ClientHandler(client, dispatcher);
        this.pool.execute(handler);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}