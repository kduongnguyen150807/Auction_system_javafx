package com.auction.server.handler.chat;

import com.auction.server.dao.chat.ChatDao;
import com.auction.server.dao.user.UserDao;
import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.service.auction.AuctionManager;
import com.auction.shared.ChatMessage;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.List;
import java.util.Map;

public class ChatHandler implements ActionHandler {
  private static final int DEFAULT_HISTORY_LIMIT = 100;
  private static final int MAX_MESSAGE_LENGTH = 1000;

  private final ChatDao chatDao = new ChatDao();
  private final UserDao userDao = new UserDao();

  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String requestId = request.getRequestId();
    User currentUser = context.getCurrentUser();

    if (currentUser == null) {
      return new Response(requestId, Response.ERROR, "not_logged_in", null);
    }

    switch (action) {
      case Request.SEND_CHAT:
        return handleSendChat(request, context);

      case Request.GET_GLOBAL_CHAT_HISTORY:
        return handleGetGlobalHistory(requestId);

      case Request.GET_PRIVATE_CHAT_HISTORY: {
        try {
          Map<String, Object> data = (Map<String, Object>) request.getPayload();

          int myId = currentUser.getId();
          int otherId = ((Number) data.get("otherId")).intValue();

          if (otherId <= 0 || userDao.getById(String.valueOf(otherId)) == null) {
            return new Response(requestId, Response.ERROR, "receiver_not_found", null);
          }

          List<ChatMessage> history =
                  chatDao.getPrivateHistory(myId, otherId, DEFAULT_HISTORY_LIMIT);

          return new Response(requestId, Response.OK, "success", history);
        } catch (Exception e) {
          return new Response(requestId, Response.ERROR, "invalid_private_chat_request", null);
        }
      }

      case Request.GET_CHAT_CONTACTS: {
        int userId = currentUser.getId();

        List<Integer> contactIds = chatDao.getContactIds(userId);
        java.util.List<User> contacts = new java.util.ArrayList<>();

        for (int id : contactIds) {
          User user = userDao.getById(String.valueOf(id));
          if (user != null) {
            user.setPassword("");
            contacts.add(user);
          }
        }

        return new Response(requestId, Response.OK, "success", (java.io.Serializable) contacts);
      }

      default:
        return new Response(requestId, Response.ERROR, "Unknown chat action", null);
    }
  }

  private Response handleSendChat(Request request, HandlerContext context) {
    ChatMessage msg = (ChatMessage) request.getPayload();
    User sender = context.getCurrentUser();

    if (sender == null) {
      return new Response(request.getRequestId(), Response.ERROR, "Unauthorized", null);
    }

    if (msg == null) {
      return new Response(request.getRequestId(), Response.ERROR, "Invalid message", null);
    }

    String content = msg.getContent() == null ? "" : msg.getContent().trim();

    if (content.isEmpty()) {
      return new Response(request.getRequestId(), Response.ERROR, "Empty message", null);
    }

    if (content.length() > MAX_MESSAGE_LENGTH) {
      return new Response(request.getRequestId(), Response.ERROR, "Message too long", null);
    }

    String messageType = msg.getMessageType();

    if (!ChatMessage.TYPE_GLOBAL.equals(messageType)
            && !ChatMessage.TYPE_PRIVATE.equals(messageType)) {
      return new Response(request.getRequestId(), Response.ERROR, "Invalid message type", null);
    }

    msg.setContent(content);
    msg.setSenderId(sender.getId());
    msg.setSenderUsername(sender.getUsername());
    msg.setSenderAvatarUrl(sender.getAvatarUrl());

    if (ChatMessage.TYPE_GLOBAL.equals(messageType)) {
      msg.setReceiverId(0);
    } else {
      int receiverId = msg.getReceiverId();

      if (receiverId <= 0
              || receiverId == sender.getId()
              || userDao.getById(String.valueOf(receiverId)) == null) {
        return new Response(request.getRequestId(), Response.ERROR, "Invalid receiver", null);
      }
    }

    if (chatDao.insertMessage(msg)) {
      if (ChatMessage.TYPE_GLOBAL.equals(msg.getMessageType())) {
        AuctionManager.getInstance()
                .broadcast(new Response("", "CHAT_GLOBAL", "new_message", msg));
      } else {
        AuctionManager.getInstance()
                .sendToUser(msg.getReceiverId(), new Response("", "CHAT_PRIVATE", "new_message", msg));
        AuctionManager.getInstance()
                .sendToUser(msg.getSenderId(), new Response("", "CHAT_PRIVATE", "new_message", msg));
      }

      return new Response(request.getRequestId(), Response.OK, "sent", msg);
    }

    return new Response(request.getRequestId(), Response.ERROR, "Failed to send", null);
  }

  private Response handleGetGlobalHistory(String requestId) {
    return new Response(
            requestId,
            Response.OK,
            "success",
            chatDao.getGlobalHistory(DEFAULT_HISTORY_LIMIT));
  }
}