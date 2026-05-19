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
import org.slf4j.MDC;

public class ClientHandler implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

  private static final int MAX_PACKET_SIZE = 10 * 1024 * 1024;
  private static final int MAX_TOKENS = 100;
  private static final String DEFAULT_REQUEST_ID = "SYSTEM";
  private static final String RATE_LIMIT_ERROR = "rate_limit_exceeded";

  private final Socket socket;
  private final DataInputStream in;
  private final DataOutputStream out;
  private final HandlerContext context;
  private final ActionRegistry registry;
  private final TokenBucket bucket;
  private final ObjectMapper jsonMapper;

  public ClientHandler(Socket socket) throws IOException {
    this.socket = socket;
    this.bucket = new TokenBucket(MAX_TOKENS);
    this.jsonMapper = createObjectMapper();
    this.in = new DataInputStream(socket.getInputStream());
    this.out = new DataOutputStream(socket.getOutputStream());
    this.context = createHandlerContext();
    this.registry = buildRegistry();

    AuctionManager.getInstance().addClient(this);
  }

  private ObjectMapper createObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType(Object.class)
            .build();

    mapper.activateDefaultTyping(
            ptv,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
    );

    return mapper;
  }

  private HandlerContext createHandlerContext() {
    UserService userService = new UserService();
    ItemDao itemDao = new ItemDao();
    LotDao lotDao = new LotDao();
    TransactionLogDao logDao = new TransactionLogDao();
    RatingDao ratingDao = new RatingDao();

    return new HandlerContext(userService, itemDao, lotDao, logDao, ratingDao, this);
  }

  private ActionRegistry buildRegistry() {
    ActionRegistry reg = new ActionRegistry();

    registerAuthHandlers(reg);
    registerAuctionHandlers(reg);
    registerMiscHandlers(reg);
    registerChatHandlers(reg);
    registerUserHandlers(reg);
    registerProtectedHandlers(reg);
    registerAdminHandlers(reg);

    reg.register(Request.GET_LEADERBOARD, new com.auction.server.handler.misc.LeaderboardHandler());

    return reg;
  }

  private void registerAuthHandlers(ActionRegistry reg) {
    reg.register(Request.LOGIN, new LoginHandler());
    reg.register(Request.SIGNUP, new SignupHandler());
    reg.register(Request.RECONNECT, new ReconnectHandler());

    ForgotPasswordHandler forgotPasswordHandler = new ForgotPasswordHandler();
    reg.register(Request.FORGOT_PASSWORD_REQ, forgotPasswordHandler);
    reg.register(Request.FORGOT_PASSWORD_RESET, forgotPasswordHandler);
  }

  private void registerAuctionHandlers(ActionRegistry reg) {
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
    reg.register(Request.GET_WATCHLIST_ITEMS, lotQuery);

    ItemQueryHandler itemQuery = new ItemQueryHandler();
    reg.register(Request.GET_MY_ITEMS, itemQuery);
    reg.register(Request.GET_ITEM_BY_ID, itemQuery);
  }

  private void registerMiscHandlers(ActionRegistry reg) {
    RatingHandler ratingHandler = new RatingHandler();
    reg.register(Request.GET_RATINGS, ratingHandler);

    MiscHandler miscHandler = new MiscHandler();
    reg.register(Request.REFRESH_USER, miscHandler);
    reg.register(Request.GET_TRANSACTIONS, miscHandler);
    reg.register(Request.GET_BID_HISTORY, miscHandler);
    reg.register(Request.PING, miscHandler);
  }

  private void registerChatHandlers(ActionRegistry reg) {
    ChatHandler chatHandler = new ChatHandler();
    reg.register(Request.GET_GLOBAL_CHAT_HISTORY, chatHandler);
    reg.register(Request.GET_PRIVATE_CHAT_HISTORY, chatHandler);
    reg.register(Request.GET_CHAT_CONTACTS, chatHandler);
  }

  private void registerUserHandlers(ActionRegistry reg) {
    UserManagementHandler userManagementHandler = new UserManagementHandler();
    reg.register(Request.GET_ALL_USERS, userManagementHandler);
    reg.register(Request.SEARCH_USERS, userManagementHandler);
    reg.register(Request.GET_USER_BY_ID, userManagementHandler);

    FriendHandler friendHandler = new FriendHandler();
    reg.register(Request.GET_FRIENDS, friendHandler);
    reg.register(Request.GET_FRIEND_REQUESTS, friendHandler);
  }

  private void registerProtectedHandlers(ActionRegistry reg) {
    RatingHandler ratingHandler = new RatingHandler();
    ChatHandler chatHandler = new ChatHandler();
    FriendHandler friendHandler = new FriendHandler();

    WatchlistHandler watchlistHandler = new WatchlistHandler();
    reg.register(Request.GET_WATCHLIST, ActionHandler.requireAuth(watchlistHandler));
    reg.register(Request.TOGGLE_WATCHLIST, ActionHandler.requireAuth(watchlistHandler));

    reg.register(Request.BID, ActionHandler.requireAuth(new BidHandler()));
    reg.register(Request.ADD_LOT, ActionHandler.requireAuth(new AddLotHandler()));
    reg.register(Request.SELLER_CANCEL_ITEM, ActionHandler.requireAuth(new SellerCancelItemHandler()));
    reg.register(
            Request.SELLER_UPDATE_PENDING_ITEM,
            ActionHandler.requireAuth(new SellerUpdatePendingItemHandler())
    );
    reg.register(Request.UPDATE_PROFILE, ActionHandler.requireAuth(new UpdateProfileHandler()));
    reg.register(Request.UPDATE_AVATAR, ActionHandler.requireAuth(new UpdateAvatarHandler()));
    reg.register(Request.DEPOSIT, ActionHandler.requireAuth(new DepositHandler()));
    reg.register(Request.SUBMIT_RATING, ActionHandler.requireAuth(ratingHandler));
    reg.register(Request.SEND_CHAT, ActionHandler.requireAuth(chatHandler));

    reg.register(Request.ADD_FRIEND, ActionHandler.requireAuth(friendHandler));
    reg.register(Request.ACCEPT_FRIEND, ActionHandler.requireAuth(friendHandler));
    reg.register(Request.DECLINE_FRIEND, ActionHandler.requireAuth(friendHandler));
    reg.register(Request.REMOVE_FRIEND, ActionHandler.requireAuth(friendHandler));
  }

  private void registerAdminHandlers(ActionRegistry reg) {
    UserManagementHandler userManagementHandler = new UserManagementHandler();
    ItemQueryHandler itemQuery = new ItemQueryHandler();
    MiscHandler miscHandler = new MiscHandler();

    reg.register(Request.LOCK_USER, ActionHandler.requireAdmin(userManagementHandler));
    reg.register(Request.UNLOCK_USER, ActionHandler.requireAdmin(userManagementHandler));
    reg.register(Request.PROMOTE_ADMIN, ActionHandler.requireAdmin(userManagementHandler));
    reg.register(Request.GET_PENDING_ITEMS, ActionHandler.requireAdmin(itemQuery));
    reg.register(Request.APPROVE_ITEM, ActionHandler.requireAdmin(itemQuery));
    reg.register(Request.REJECT_ITEM, ActionHandler.requireAdmin(itemQuery));
    reg.register(Request.GET_STATUS_STATS, ActionHandler.requireAdmin(miscHandler));
    reg.register(Request.GET_CATEGORY_STATS, ActionHandler.requireAdmin(miscHandler));
  }

  public User getCurrentUser() {
    return this.context.getCurrentUser();
  }

  @Override
  public void run() {
    try {
      while (!socket.isClosed()) {
        Request request = readRequest();
        processRequest(request);
      }
    } catch (EOFException e) {
      logger.info("Client disconnected: {}", socket.getRemoteSocketAddress());
    } catch (Exception e) {
      logger.error("Read error from client {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
    } finally {
      cleanup();
    }
  }

  private Request readRequest() throws IOException {
    int length = in.readInt();
    validatePacketLength(length);

    byte[] payload = new byte[length];
    in.readFully(payload);

    String jsonRequest = new String(payload, StandardCharsets.UTF_8);
    return jsonMapper.readValue(jsonRequest, Request.class);
  }

  private void validatePacketLength(int length) throws IOException {
    if (length <= 0 || length > MAX_PACKET_SIZE) {
      throw new IOException("Invalid packet length: " + length);
    }
  }

  private void processRequest(Request request) {
    MDC.put("reqId", getRequestIdOrDefault(request));

    try {
      if (!bucket.tryConsume()) {
        send(new Response(request.getRequestId(), Response.ERROR, RATE_LIMIT_ERROR, null));
        return;
      }

      Response response = registry.dispatch(request, context);
      if (response != null) {
        send(response);
      }
    } finally {
      MDC.clear();
    }
  }

  private String getRequestIdOrDefault(Request request) {
    if (request.getRequestId() == null) {
      return DEFAULT_REQUEST_ID;
    }
    return request.getRequestId();
  }

  public void send(Response response) {
    try {
      byte[] data = serializeResponse(response);
      writeResponse(data);
    } catch (IOException e) {
      logger.error("Failed to send response to {}: {}", socket.getRemoteSocketAddress(), e.getMessage());
      closeSocketQuietly();
    }
  }

  private byte[] serializeResponse(Response response) throws IOException {
    String jsonResponse = jsonMapper.writeValueAsString(response);
    return jsonResponse.getBytes(StandardCharsets.UTF_8);
  }

  private void writeResponse(byte[] data) throws IOException {
    synchronized (out) {
      out.writeInt(data.length);
      out.write(data);
      out.flush();
    }
  }

  private void cleanup() {
    AuctionManager.getInstance().removeClient(this);
    closeSocketQuietly();
  }

  private void closeSocketQuietly() {
    try {
      if (!socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      logger.error("Error closing socket: {}", e.getMessage());
    }
  }
}