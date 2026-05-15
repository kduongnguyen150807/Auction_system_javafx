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
    this.registry = ActionRegistryFactory.create();
    AuctionManager.getInstance().addClient(this);
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
          if (!bucket.tryConsume()) {
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