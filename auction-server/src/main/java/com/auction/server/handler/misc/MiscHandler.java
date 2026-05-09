package com.auction.server.handler.misc;

import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.user.UserDao;
import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.BidTransaction;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.TransactionLog;
import com.auction.shared.User;
import java.util.List;
//Thêm case xử lý LOGOUT để Server xóa session)
public class MiscHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String requestid = request.getRequestId();
    Object payload = request.getPayload();

    switch (action) {
      case "LOGOUT":
        context.setCurrentUser(null);
        Response ans = new Response(requestid, Response.OK, "logged_out", null);
        return ans;

      case Request.REFRESH_USER:
        User user = new UserDao().getById(String.valueOf(payload));
        Response res1 = new Response(
                requestid,
                user != null ? Response.OK : Response.ERROR,
                user != null ? "success" : "fail",
                user);
        return res1;

      case Request.GET_TRANSACTIONS:
        List<TransactionLog> logs = context.getLogDao().getByUserId((int) payload);
        Response res2 = new Response(requestid, Response.OK, "success", (java.io.Serializable) logs);
        return res2;

      case Request.GET_STATUS_STATS:
        Response res3 = new Response(requestid, Response.OK, "success", context.getItemDao().getStatusStats());
        return res3;

      case Request.GET_CATEGORY_STATS:
        Response res4 = new Response(requestid, Response.OK, "success", context.getItemDao().getCategoryStats());
        return res4;

      case Request.GET_BID_HISTORY:
        int itemid = (int) payload;
        List<BidTransaction> history = new BidDao().getBidHistory(itemid);
        Response res5 = new Response(requestid, Response.OK, "success", (java.io.Serializable) history);
        return res5;

      case Request.PING:
        Response res6 = new Response(requestid, Response.OK, "pong", null);
        return res6;

      default:
        Response res7 = new Response(requestid, Response.ERROR, "unknown_action", null);
        return res7;
    }
  }
}