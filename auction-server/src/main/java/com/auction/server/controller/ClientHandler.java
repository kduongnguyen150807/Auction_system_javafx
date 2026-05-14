package com.auction.server.controller;

import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.auction.LotDao;
import com.auction.server.dao.rating.RatingDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.server.handler.auction.*;
import com.auction.server.handler.auth.*;
import com.auction.server.handler.chat.*;
import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.ActionRegistry;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.handler.misc.MiscHandler;
import com.auction.server.handler.rating.RatingHandler;
import com.auction.server.handler.user.*;
import com.auction.server.service.auction.AuctionManager;
import com.auction.server.service.user.UserService;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC; // IMPORT MDC

public class ClientHandler implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

  private final Socket socket;
  private final DataInputStream in;
  private final DataOutputStream out;
  private final HandlerContext context;
  private final ActionRegistry registry;
  private final TokenBucket bucket;
  private final ObjectMapper jsonMapper;

  public ClientHandler(Socket socket) throws IOException {
    this.socket = socket;
    this.bucket = new TokenBucket(100);

    this.jsonMapper = new ObjectMapper();
    this.jsonMapper.registerModule(new JavaTimeModule());
    this.jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType(Object.class)
            .build();
    this.jsonMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

    this.in = new DataInputStream(socket.getInputStream());
    this.out = new DataOutputStream(socket.getOutputStream());

    UserService userService = new UserService();
    ItemDao itemDao = new ItemDao();
    LotDao lotDao = new LotDao();
    TransactionLogDao logDao = new TransactionLogDao();
    RatingDao ratingDao = new RatingDao();

    this.context = new HandlerContext(userService, itemDao, lotDao, logDao, ratingDao, this);
    this.registry = buildRegistry();

    AuctionManager.getInstance().addClient(this);
  }

  private ActionRegistry buildRegistry() {
    ActionRegistry reg = new ActionRegistry();

    reg.register(Request.LOGIN, new LoginHandler());
    reg.register(Request.SIGNUP, new SignupHandler());
    reg.register(Request.RECONNECT, new ReconnectHandler());

    reg.register(Request.AUTOCOMPLETE, new AutocompleteHandler());
    ListItemsHandler listHandler = new ListItemsHandler();
    reg.register(Request.LIST, listHandler);
    reg.register(Request.GET_ONGOING_LOTS, listHandler);

    LotQueryHandler lotQuery = new LotQueryHandler();
    reg.register(Request.GET_ONGOING_BIDS, lotQuery);
    reg.register(Request.GET_TRENDING_LOTS, lotQuery);
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

    reg.register(Request.BID, ActionHandler.requireAuth(new BidHandler()));
    reg.register(Request.ADD_LOT, ActionHandler.requireAuth(new AddLotHandler()));
    reg.register(Request.SELLER_CANCEL_ITEM, ActionHandler.requireAuth(new SellerCancelItemHandler()));
    reg.register(
        Request.SELLER_UPDATE_PENDING_ITEM,
        ActionHandler.requireAuth(new SellerUpdatePendingItemHandler()));
    reg.register(Request.UPDATE_PROFILE, ActionHandler.requireAuth(new UpdateProfileHandler()));
    reg.register(Request.UPDATE_AVATAR, ActionHandler.requireAuth(new UpdateAvatarHandler()));
    reg.register(Request.DEPOSIT, ActionHandler.requireAuth(new DepositHandler()));
    reg.register(Request.SUBMIT_RATING, ActionHandler.requireAuth(ratingHandler));
    reg.register(Request.SEND_CHAT, ActionHandler.requireAuth(chatHandler));

    reg.register(Request.ADD_FRIEND, ActionHandler.requireAuth(friendHandler));
    reg.register(Request.ACCEPT_FRIEND, ActionHandler.requireAuth(friendHandler));
    reg.register(Request.DECLINE_FRIEND, ActionHandler.requireAuth(friendHandler));
    reg.register(Request.REMOVE_FRIEND, ActionHandler.requireAuth(friendHandler));

    reg.register(Request.LOCK_USER, ActionHandler.requireAdmin(userMgmt));
    reg.register(Request.UNLOCK_USER, ActionHandler.requireAdmin(userMgmt));
    reg.register(Request.PROMOTE_ADMIN, ActionHandler.requireAdmin(userMgmt));
    reg.register(Request.GET_PENDING_ITEMS, ActionHandler.requireAdmin(itemQuery));
    reg.register(Request.APPROVE_ITEM, ActionHandler.requireAdmin(itemQuery));
    reg.register(Request.REJECT_ITEM, ActionHandler.requireAdmin(itemQuery));
    reg.register(Request.GET_STATUS_STATS, ActionHandler.requireAdmin(miscHandler));
    reg.register(Request.GET_CATEGORY_STATS, ActionHandler.requireAdmin(miscHandler));

    reg.register(Request.GET_LEADERBOARD, new com.auction.server.handler.misc.LeaderboardHandler());

    return reg;
  }

  public User getCurrentUser() {
    return this.context.getCurrentUser();
  }

  @Override
  public void run() {
    try {
      while (!socket.isClosed()) {
        int length = in.readInt();
        if (length <= 0 || length > 10 * 1024 * 1024) {
          throw new IOException("Invalid packet length: " + length);
        }

        byte[] payload = new byte[length];
        in.readFully(payload);

        String jsonReq = new String(payload, StandardCharsets.UTF_8);
        Request request = jsonMapper.readValue(jsonReq, Request.class);

        // TÍNH NĂNG 4: MDC LOGGING - Gắn RequestID vào Thread hiện tại
        if (request.getRequestId() != null) {
          MDC.put("reqId", request.getRequestId());
        } else {
          MDC.put("reqId", "SYSTEM");
        }

        try {
          if (!bucket.tryconsume()) {
            send(new Response(request.getRequestId(), Response.ERROR, "rate_limit_exceeded", null));
            continue;
          }

          Response res = this.registry.dispatch(request, this.context);
          if (res != null) {
            send(res);
          }
        } finally {
          // Bắt buộc clear MDC để không bị rò rỉ ID sang Request tiếp theo của cùng Thread
          MDC.clear();
        }
      }
    } catch (EOFException e) {
      logger.info("Client disconnected: {}", socket.getRemoteSocketAddress());
    } catch (Exception e) {
      logger.error("Read error from client {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
    } finally {
      cleanup();
    }
  }

  public void send(Response response) {
    try {
      String jsonRes = jsonMapper.writeValueAsString(response);
      byte[] data = jsonRes.getBytes(StandardCharsets.UTF_8);

      synchronized (out) {
        out.writeInt(data.length);
        out.write(data);
        out.flush();
      }
    } catch (IOException e) {
      logger.error("Failed to send response to {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
    }
  }

  private void cleanup() {
    AuctionManager.getInstance().removeClient(this);
    try {
      if (!socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      logger.error("Error closing socket: {}", e.getMessage());
    }
  }
}