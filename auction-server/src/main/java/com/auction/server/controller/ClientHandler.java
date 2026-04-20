package com.auction.server.controller;

import com.auction.server.dao.*;
import com.auction.server.handler.*;
import com.auction.server.service.AuctionManager;
import com.auction.server.service.UserService;
import com.auction.shared.*;
import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
  private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

  private final Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;
  private final HandlerContext context;
  private final ActionRegistry registry;

  public ClientHandler(Socket socket) {
    this.socket = socket;

    UserService userService = new UserService();
    ItemDao itemDao = new ItemDao();
    LotDao lotDao = new LotDao();
    TransactionLogDao logDao = new TransactionLogDao();
    RatingDao ratingDao = new RatingDao();

    this.context = new HandlerContext(userService, itemDao, lotDao, logDao, ratingDao, this);
    this.registry = buildRegistry();

    try {
      this.out = new ObjectOutputStream(this.socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.socket.getInputStream());
      AuctionManager.getInstance().addClient(this);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Failed to initialize client handler streams", e);
    }
  }

  private ActionRegistry buildRegistry() {
    ActionRegistry reg = new ActionRegistry();

    LoginHandler loginHandler = new LoginHandler();
    reg.register(Request.LOGIN, loginHandler);
    reg.register(Request.SIGNUP, new SignupHandler());

    ListItemsHandler listHandler = new ListItemsHandler();
    reg.register(Request.LIST, listHandler);
    reg.register(Request.GET_ONGOING_LOTS, listHandler);

    reg.register(Request.BID, new BidHandler());
    reg.register(Request.ADD_LOT, new AddLotHandler());
    reg.register(Request.UPDATE_PROFILE, new UpdateProfileHandler());
    reg.register(Request.UPDATE_AVATAR, new UpdateAvatarHandler());

    UserManagementHandler userMgmt = new UserManagementHandler();
    reg.register(Request.GET_ALL_USERS, userMgmt);
    reg.register(Request.LOCK_USER, userMgmt);
    reg.register(Request.UNLOCK_USER, userMgmt);
    reg.register(Request.PROMOTE_ADMIN, userMgmt);
    reg.register(Request.SEARCH_USERS, userMgmt);
    reg.register(Request.GET_USER_BY_ID, userMgmt);

    LotQueryHandler lotQuery = new LotQueryHandler();
    reg.register(Request.GET_ONGOING_BIDS, lotQuery);
    reg.register(Request.GET_UPCOMING_BIDS, lotQuery);
    reg.register(Request.GET_CLOSED_BIDS, lotQuery);
    reg.register(Request.GET_PAST_BIDS, lotQuery);

    ItemQueryHandler itemQuery = new ItemQueryHandler();
    reg.register(Request.GET_MY_ITEMS, itemQuery);
    reg.register(Request.GET_ITEM_BY_ID, itemQuery);
    reg.register(Request.GET_PENDING_ITEMS, itemQuery);
    reg.register(Request.APPROVE_ITEM, itemQuery);
    reg.register(Request.REJECT_ITEM, itemQuery);

    RatingHandler ratingHandler = new RatingHandler();
    reg.register(Request.SUBMIT_RATING, ratingHandler);
    reg.register(Request.GET_RATINGS, ratingHandler);

    reg.register(Request.DEPOSIT, new DepositHandler());

    MiscHandler miscHandler = new MiscHandler();
    reg.register(Request.REFRESH_USER, miscHandler);
    reg.register(Request.GET_TRANSACTIONS, miscHandler);
    reg.register(Request.GET_STATUS_STATS, miscHandler);
    reg.register(Request.GET_CATEGORY_STATS, miscHandler);
    reg.register(Request.GET_BID_HISTORY, miscHandler);
    reg.register(Request.PING, miscHandler);

    ChatHandler chatHandler = new ChatHandler();
    reg.register(Request.SEND_CHAT, chatHandler);
    reg.register(Request.GET_GLOBAL_CHAT_HISTORY, chatHandler);
    reg.register(Request.GET_PRIVATE_CHAT_HISTORY, chatHandler);
    reg.register(Request.GET_CHAT_CONTACTS, chatHandler);

    FriendHandler friendHandler = new FriendHandler();
    reg.register(Request.ADD_FRIEND, friendHandler);
    reg.register(Request.ACCEPT_FRIEND, friendHandler);
    reg.register(Request.DECLINE_FRIEND, friendHandler);
    reg.register(Request.REMOVE_FRIEND, friendHandler);
    reg.register(Request.GET_FRIENDS, friendHandler);
    reg.register(Request.GET_FRIEND_REQUESTS, friendHandler);

    return reg;
  }

  public User getCurrentUser() {
    return this.context.getCurrentUser();
  }

  @Override
  public void run() {
    try {
      while (true) {
        Request request = (Request) this.in.readObject();
        Response response = this.registry.dispatch(request, this.context);
        if (response != null) {
          synchronized (this.out) {
            this.out.reset();
            this.out.writeObject(response);
            this.out.flush();
          }
        }
      }
    } catch (EOFException e) {
      LOGGER.fine("Client disconnected");
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Error in client handler loop", e);
    } finally {
      AuctionManager.getInstance().removeClient(this);
      try {
        this.socket.close();
      } catch (Exception e) {
        LOGGER.log(Level.FINE, "Error closing socket", e);
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
      LOGGER.log(Level.WARNING, "Failed to send response to client", e);
    }
  }
}
