package com.auction.server.handler;

import com.auction.server.dao.ChatDao;
import com.auction.server.dao.UserDao;
import com.auction.server.service.AuctionManager;
import com.auction.shared.*;
import java.util.List;
import java.util.Map;

public class ChatHandler implements ActionHandler {
  private final ChatDao chatDao = new ChatDao();
  private final UserDao userDao = new UserDao();

  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String requestId = request.getRequestId();

    switch (action) {
      case Request.SEND_CHAT:
        return handleSendChat(request, context);

      case Request.GET_GLOBAL_CHAT_HISTORY:
        return handleGetGlobalHistory(requestId);

      case Request.GET_PRIVATE_CHAT_HISTORY: {
        Map<String, Object> data = (Map<String, Object>) request.getPayload();
        int myId = ((Number) data.get("myId")).intValue();
        int otherId = ((Number) data.get("otherId")).intValue();
        List<ChatMessage> history = chatDao.getPrivateHistory(myId, otherId, 100);
        return new Response(requestId, Response.OK, "success", (java.io.Serializable) history);
      }

      case Request.GET_CHAT_CONTACTS: {
        int userId = ((Number) request.getPayload()).intValue();
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
        return new Response(requestId, Response.ERROR, "unknown_chat_action", null);
    }
  }

  private Response handleSendChat(Request request, HandlerContext context) {
    ChatMessage msg = (ChatMessage) request.getPayload();
    User sender = context.getCurrentUser();
    msg.setSenderId(sender.getId());
    msg.setSenderUsername(sender.getUsername());
    msg.setSenderAvatarUrl(sender.getAvatarUrl());

    String content = msg.getContent();
    if (content == null || content.trim().isEmpty()) {
      return new Response(request.getRequestId(), Response.ERROR, "Empty message", null);
    }
    if (content.length() > 500) {
      msg.setContent(content.substring(0, 500));
    }

    boolean saved = chatDao.insertMessage(msg);
    if (!saved) {
      return new Response(request.getRequestId(), Response.ERROR, "Failed to save", null);
    }

    if (ChatMessage.TYPE_GLOBAL.equals(msg.getMessageType())) {
      AuctionManager.getInstance().broadcast(
          new Response("", "CHAT_GLOBAL", "new_message", msg));
    } else if (ChatMessage.TYPE_PRIVATE.equals(msg.getMessageType())) {
      AuctionManager.getInstance().sendToUser(msg.getReceiverId(),
          new Response("", "CHAT_PRIVATE", "new_message", msg));
      AuctionManager.getInstance().sendToUser(msg.getSenderId(),
          new Response("", "CHAT_PRIVATE", "new_message", msg));
    }

    return new Response(request.getRequestId(), Response.OK, "sent", msg);
  }

  private Response handleGetGlobalHistory(String requestId) {
    List<ChatMessage> history = chatDao.getGlobalHistory(100);
    return new Response(requestId, Response.OK, "success", (java.io.Serializable) history);
  }
}
