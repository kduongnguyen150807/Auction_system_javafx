package com.auction.server.controller;

import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.auction.LotDao;
import com.auction.server.dao.rating.RatingDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.server.handler.dispatch.ActionRegistry;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.service.auction.AuctionManager;
import com.auction.server.service.user.UserService;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

  private final Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;

  private final HandlerContext context;
  private final ActionRegistry registry;
  private final TokenBucket bucket;

  public ClientHandler(Socket socket) {
    this.socket = socket;
    this.bucket = new TokenBucket(100);

    UserService userService = new UserService();
    ItemDao itemDao = new ItemDao();
    LotDao lotDao = new LotDao();
    TransactionLogDao transactionLogDao = new TransactionLogDao();
    RatingDao ratingDao = new RatingDao();

    this.context = new HandlerContext(
            userService,
            itemDao,
            lotDao,
            transactionLogDao,
            ratingDao,
            this
    );

    this.registry = ActionRegistryFactory.create();
    try {
      this.out = new ObjectOutputStream(this.socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.socket.getInputStream());

      AuctionManager.getInstance().addClient(this);
    } catch (Exception e) {
      logger.error("client_handler_init_error", e);
    }
  }

  public User getCurrentUser() {
    return this.context.getCurrentUser();
  }

  @Override
  public void run() {
    try {
      while (true) {
        Request request = (Request) this.in.readObject();

        if (!bucket.tryConsume()) {
          Response response = new Response(
                  request.getRequestId(),
                  Response.ERROR,
                  "rate_limit_exceeded",
                  null
          );
          send(response);
          continue;
        }

        Response response = this.registry.dispatch(request, this.context);

        if (response != null) {
          send(response);
        }
      }
    } catch (EOFException e) {
      logger.info("client_disconnected");
    } catch (Exception e) {
      logger.error("client_read_error", e);
    } finally {
      AuctionManager.getInstance().removeClient(this);

      try {
        this.socket.close();
      } catch (Exception e) {
        logger.error("socket_close_error", e);
      }
    }
  }

  public void send(Response response) {
    try {
      synchronized (this.out) {
        this.out.reset();
        this.out.writeObject(response);
        this.out.flush();
      }
    } catch (Exception e) {
      logger.error("client_send_error", e);
    }
  }
}