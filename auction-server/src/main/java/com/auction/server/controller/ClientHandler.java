package com.auction.server.controller;

import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.auction.LotDao;
import com.auction.server.dao.rating.RatingDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.server.handler.auction.AddLotHandler;
import com.auction.server.handler.auction.AutocompleteHandler;
import com.auction.server.handler.auction.BidHandler;
import com.auction.server.handler.auction.ItemQueryHandler;
import com.auction.server.handler.auction.ListItemsHandler;
import com.auction.server.handler.auction.LotQueryHandler;
import com.auction.server.handler.auth.LoginHandler;
import com.auction.server.handler.auth.ReconnectHandler;
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
    UserService userservice = new UserService();
    ItemDao itemdao = new ItemDao();
    LotDao lotdao = new LotDao();
    TransactionLogDao logdao = new TransactionLogDao();
    RatingDao ratingdao = new RatingDao();
    this.context = new HandlerContext(userservice, itemdao, lotdao, logdao, ratingdao, this);
    this.registry = buildregistry();
    try {
      this.out = new ObjectOutputStream(this.socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.socket.getInputStream());
      AuctionManager.getInstance().addClient(this);
    } catch (Exception e) {
      logger.error("client_handler_init_error", e);
    }
  }

  private ActionRegistry buildregistry() {
    ActionRegistry reg = new ActionRegistry();
    reg.register(Request.LOGIN, new LoginHandler());
    reg.register(Request.SIGNUP, new SignupHandler());
    reg.register(Request.RECONNECT, new ReconnectHandler());
    reg.register(Request.AUTOCOMPLETE, new AutocompleteHandler());
    ListItemsHandler listhandler = new ListItemsHandler();
    reg.register(Request.LIST, listhandler);
    reg.register(Request.GET_ONGOING_LOTS, listhandler);
    LotQueryHandler lotquery = new LotQueryHandler();
    reg.register(Request.GET_ONGOING_BIDS, lotquery);
    reg.register(Request.GET_UPCOMING_BIDS, lotquery);
    reg.register(Request.GET_CLOSED_BIDS, lotquery);
    reg.register(Request.GET_PAST_BIDS, lotquery);
    ItemQueryHandler itemquery = new ItemQueryHandler();
    reg.register(Request.GET_MY_ITEMS, itemquery);
    reg.register(Request.GET_ITEM_BY_ID, itemquery);
    RatingHandler ratinghandler = new RatingHandler();
    reg.register(Request.GET_RATINGS, ratinghandler);
    MiscHandler mischandler = new MiscHandler();
    reg.register(Request.REFRESH_USER, mischandler);
    reg.register(Request.GET_TRANSACTIONS, mischandler);
    reg.register(Request.GET_BID_HISTORY, mischandler);
    reg.register(Request.PING, mischandler);
    ChatHandler chathandler = new ChatHandler();
    reg.register(Request.GET_GLOBAL_CHAT_HISTORY, chathandler);
    reg.register(Request.GET_PRIVATE_CHAT_HISTORY, chathandler);
    reg.register(Request.GET_CHAT_CONTACTS, chathandler);
    UserManagementHandler usermgmt = new UserManagementHandler();
    reg.register(Request.GET_ALL_USERS, usermgmt);
    reg.register(Request.SEARCH_USERS, usermgmt);
    reg.register(Request.GET_USER_BY_ID, usermgmt);
    FriendHandler friendhandler = new FriendHandler();
    reg.register(Request.GET_FRIENDS, friendhandler);
    reg.register(Request.GET_FRIEND_REQUESTS, friendhandler);
    reg.register(Request.BID, ActionHandler.requireAuth(new BidHandler()));
    reg.register(Request.ADD_LOT, ActionHandler.requireAuth(new AddLotHandler()));
    reg.register(Request.UPDATE_PROFILE, ActionHandler.requireAuth(new UpdateProfileHandler()));
    reg.register(Request.UPDATE_AVATAR, ActionHandler.requireAuth(new UpdateAvatarHandler()));
    reg.register(Request.DEPOSIT, ActionHandler.requireAuth(new DepositHandler()));
    reg.register(Request.SUBMIT_RATING, ActionHandler.requireAuth(ratinghandler));
    reg.register(Request.SEND_CHAT, ActionHandler.requireAuth(chathandler));
    FriendHandler authfriend = friendhandler;
    reg.register(Request.ADD_FRIEND, ActionHandler.requireAuth(authfriend));
    reg.register(Request.ACCEPT_FRIEND, ActionHandler.requireAuth(authfriend));
    reg.register(Request.DECLINE_FRIEND, ActionHandler.requireAuth(authfriend));
    reg.register(Request.REMOVE_FRIEND, ActionHandler.requireAuth(authfriend));
    reg.register(Request.LOCK_USER, ActionHandler.requireAdmin(usermgmt));
    reg.register(Request.UNLOCK_USER, ActionHandler.requireAdmin(usermgmt));
    reg.register(Request.PROMOTE_ADMIN, ActionHandler.requireAdmin(usermgmt));
    reg.register(Request.GET_PENDING_ITEMS, ActionHandler.requireAdmin(itemquery));
    reg.register(Request.APPROVE_ITEM, ActionHandler.requireAdmin(itemquery));
    reg.register(Request.REJECT_ITEM, ActionHandler.requireAdmin(itemquery));
    reg.register(Request.GET_STATUS_STATS, ActionHandler.requireAdmin(mischandler));
    reg.register(Request.GET_CATEGORY_STATS, ActionHandler.requireAdmin(mischandler));
    reg.register(Request.GET_LEADERBOARD, new com.auction.server.handler.misc.LeaderboardHandler());
    ActionRegistry ans = reg;
    return ans;
  }

  public User getCurrentUser() {
    User ans = this.context.getCurrentUser();
    return ans;
  }

  @Override
  public void run() {
    try {
      while (true) {
        Request request = (Request) this.in.readObject();
        if (!bucket.tryconsume()) {
          Response res = new Response(request.getRequestId(), Response.ERROR, "rate_limit_exceeded", null);
          send(res);
          continue;
        }
        Response res = this.registry.dispatch(request, this.context);
        if (res != null) {
          synchronized (this.out) {
            this.out.reset();
            this.out.writeObject(res);
            this.out.flush();
          }
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