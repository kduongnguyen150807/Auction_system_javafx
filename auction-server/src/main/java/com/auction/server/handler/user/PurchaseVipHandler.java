package com.auction.server.handler.user;

import com.auction.server.dao.user.UserDao;
import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import com.auction.shared.VipPlan;
import java.util.Map;

public class PurchaseVipHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    User current = context.getCurrentUser();
    if (current == null) {
      return new Response(request.getRequestId(), Response.ERROR, "Unauthorized", null);
    }

    @SuppressWarnings("unchecked")
    Map<String, String> data = (Map<String, String>) request.getPayload();
    VipPlan plan = VipPlan.fromId(data != null ? data.get("planId") : null);
    if (plan == null) {
      return new Response(request.getRequestId(), Response.ERROR, "Gói VIP không hợp lệ", null);
    }

    UserDao userDao = new UserDao();
    if (!userDao.purchaseVip(current.getId(), plan)) {
      return new Response(
          request.getRequestId(), Response.ERROR, "Số dư không đủ hoặc giao dịch thất bại", null);
    }

    context.getLogDao().insertLog(current.getId(), "VIP_PURCHASE", plan.getPrice(), 0);
    User fresh = userDao.getById(String.valueOf(current.getId()));
    if (fresh != null) {
      fresh.setPassword("");
      context.setCurrentUser(fresh);
    }
    return new Response(request.getRequestId(), Response.OK, "success", fresh);
  }
}
