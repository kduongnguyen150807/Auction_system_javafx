package com.auction.server.controller;

import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.auction.LotDao;
import com.auction.server.dao.rating.RatingDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.server.handler.auction.AddLotHandler;
import com.auction.server.handler.auction.BidHandler;
import com.auction.server.handler.auction.ItemQueryHandler;
import com.auction.server.handler.auction.ListItemsHandler;
import com.auction.server.handler.auction.LotQueryHandler;
import com.auction.server.handler.auth.LoginHandler;
import com.auction.server.handler.auth.SignupHandler;
import com.auction.server.handler.chat.ChatHandler;
import com.auction.server.handler.chat.FriendHandler;
import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.ActionRegistry;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.handler.misc.MiscHandler;
import com.auction.server.handler.rating.RatingHandler;
import com.auction.server.handler.user.DepositHandler;
import com.auction.server.handler.user.UpdateAvatarHandler;
import com.auction.server.handler.user.UpdateProfileHandler;
import com.auction.server.handler.user.UserManagementHandler;
import com.auction.server.service.auction.AuctionManager;
import com.auction.server.service.user.UserService;
import com.auction.shared.*;
import java.io.*;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

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
      LOGGER.error("Failed to initialize client handler streams", e);
    }
  }

  private ActionRegistry buildRegistry() {
    ActionRegistry reg = new ActionRegistry();

    // ── Public (no auth required) ─────────────────────────────────────────
    reg.register(Request.LOGIN, new LoginHandler());
    reg.register(Request.SIGNUP, new SignupHandler());

    ListItemsHandler listHandler = new ListItemsHandler();
    reg.register(Request.LIST, listHandler);
    reg.register(Request.GET_ONGOING_LOTS, listHandler);

    LotQueryHandler lotQuery = new LotQueryHandler();
    reg.register(Request.GET_ONGOING_BIDS, lotQuery);
    reg.register(Request.GET_UPCOMING_BIDS, lotQuery);
    reg.register(Request.GET_CLOSED_BIDS, lotQuery);
    reg.register(Request.GET_PAST_BIDS, lotQuery);

    ItemQueryHandler itemQuery = new ItemQueryHandler();
    reg.register(Request.GET_MY_ITEMS, itemQuery);
    reg.register(Request.GET_ITEM_BY_ID, itemQuery);

    RatingHandler ratingHandler = new RatingHandler();
    reg.register(Request.GET_RATINGS, ratingHandler);

    MiscHandler miscHandler = new MiscHandler();
    reg.register(Request.REFRESH_USER, miscHandler);
    reg.register(Request.GET_TRANSACTIONS, miscHandler);
    reg.register(Request.GET_BID_HISTORY, miscHandler);
    reg.register(Request.PING, miscHandler);

    ChatHandler chatHandler = new ChatHandler();
    reg.register(Request.GET_GLOBAL_CHAT_HISTORY, chatHandler);
    reg.register(Request.GET_PRIVATE_CHAT_HISTORY, chatHandler);
    reg.register(Request.GET_CHAT_CONTACTS, chatHandler);

    UserManagementHandler userMgmt = new UserManagementHandler();
    reg.register(Request.GET_ALL_USERS, userMgmt);
    reg.register(Request.SEARCH_USERS, userMgmt);
    reg.register(Request.GET_USER_BY_ID, userMgmt);

    FriendHandler friendHandler = new FriendHandler();
    reg.register(Request.GET_FRIENDS, friendHandler);
    reg.register(Request.GET_FRIEND_REQUESTS, friendHandler);

    // ── Requires login ────────────────────────────────────────────────────
    reg.register(Request.BID, ActionHandler.requireAuth(new BidHandler()));
    reg.register(Request.ADD_LOT, ActionHandler.requireAuth(new AddLotHandler()));
    reg.register(Request.UPDATE_PROFILE, ActionHandler.requireAuth(new UpdateProfileHandler()));
    reg.register(Request.UPDATE_AVATAR, ActionHandler.requireAuth(new UpdateAvatarHandler()));
    reg.register(Request.DEPOSIT, ActionHandler.requireAuth(new DepositHandler()));
    reg.register(Request.SUBMIT_RATING, ActionHandler.requireAuth(ratingHandler));
    reg.register(Request.SEND_CHAT, ActionHandler.requireAuth(chatHandler));

    FriendHandler authFriend = friendHandler;
    reg.register(Request.ADD_FRIEND, ActionHandler.requireAuth(authFriend));
    reg.register(Request.ACCEPT_FRIEND, ActionHandler.requireAuth(authFriend));
    reg.register(Request.DECLINE_FRIEND, ActionHandler.requireAuth(authFriend));
    reg.register(Request.REMOVE_FRIEND, ActionHandler.requireAuth(authFriend));

    // ── Requires ADMIN role ───────────────────────────────────────────────
    reg.register(Request.LOCK_USER, ActionHandler.requireAdmin(userMgmt));
    reg.register(Request.UNLOCK_USER, ActionHandler.requireAdmin(userMgmt));
    reg.register(Request.PROMOTE_ADMIN, ActionHandler.requireAdmin(userMgmt));
    reg.register(Request.GET_PENDING_ITEMS, ActionHandler.requireAdmin(itemQuery));
    reg.register(Request.APPROVE_ITEM, ActionHandler.requireAdmin(itemQuery));
    reg.register(Request.REJECT_ITEM, ActionHandler.requireAdmin(itemQuery));
    reg.register(Request.GET_STATUS_STATS, ActionHandler.requireAdmin(miscHandler));
    reg.register(Request.GET_CATEGORY_STATS, ActionHandler.requireAdmin(miscHandler));

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
      LOGGER.debug("Client disconnected");
    } catch (Exception e) {
      LOGGER.warn("Error in client handler loop", e);
    } finally {
      AuctionManager.getInstance().removeClient(this);
      try {
        this.socket.close();
      } catch (Exception e) {
        LOGGER.debug("Error closing socket", e);
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
      LOGGER.warn("Failed to send response to client", e);
    }
  }
}
