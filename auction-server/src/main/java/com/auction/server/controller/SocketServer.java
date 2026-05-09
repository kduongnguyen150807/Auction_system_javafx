package com.auction.server.controller;

<<<<<<< HEAD
import com.auction.server.dao.DatabaseInitializer;
import com.auction.server.service.*;
import java.io.IOException;
=======
import com.auction.server.service.auction.SettlementService;
>>>>>>> 7da57c30d1fc0b20d3aa86204c23ea6cd0d18068
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketServer {
<<<<<<< HEAD
  private final int port;
  private final ExecutorService pool;
  private final RequestDispatcher dispatcher;
=======
  private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);
  private final int port;
  private final ExecutorService pool;
>>>>>>> 7da57c30d1fc0b20d3aa86204c23ea6cd0d18068

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
<<<<<<< HEAD
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
=======
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
>>>>>>> 7da57c30d1fc0b20d3aa86204c23ea6cd0d18068
    }
  }
}