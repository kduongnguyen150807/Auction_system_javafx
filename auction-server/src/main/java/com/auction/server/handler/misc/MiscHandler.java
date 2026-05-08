package com.auction.server.handler.misc;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;

import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.user.UserDao;
import com.auction.shared.BidTransaction;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.TransactionLog;
import com.auction.shared.User;
import java.util.List;

public class MiscHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String requestId = request.getRequestId();
    Object payload = request.getPayload();

    switch (action) {
      case Request.REFRESH_USER: {
        User user = new UserDao().getById(String.valueOf(payload));
        return new Response(requestId,
            user != null ? Response.OK : Response.ERROR,
            user != null ? "success" : "fail", user);
      }

      case Request.GET_TRANSACTIONS: {
        List<TransactionLog> logs = context.getLogDao().getByUserId((int) payload);
        return new Response(requestId, Response.OK, "success", (java.io.Serializable) logs);
      }

      case Request.GET_STATUS_STATS:
        return new Response(requestId, Response.OK, "success", context.getItemDao().getStatusStats());

      case Request.GET_CATEGORY_STATS:
        return new Response(requestId, Response.OK, "success", context.getItemDao().getCategoryStats());

      case Request.GET_BID_HISTORY: {
        int itemId = (int) payload;
        List<BidTransaction> history = new BidDao().getBidHistory(itemId);
        return new Response(requestId, Response.OK, "success", (java.io.Serializable) history);
      }

      case Request.PING:
        return new Response(requestId, Response.OK, "pong", null);

      default:
        return new Response(requestId, Response.ERROR, "unknown_action", null);
    }
  }
}
