package com.auction.server.handler.user;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.dao.user.UserDao;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.Map;

public class DepositHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    Map<String, String> data = (Map<String, String>) request.getPayload();
    int userId = Integer.parseInt(data.get("userid"));
    double amount = Double.parseDouble(data.get("amount"));

    // CHẶN NẠP TIỀN ÂM
    if (amount <= 0) {
      return new Response(request.getRequestId(), Response.ERROR, "Số tiền nạp phải lớn hơn 0!", null);
    }

    UserDao userDao = new UserDao();
    if (userDao.atomicCreditBalance(userId, amount)) {
      context.getLogDao().insertLog(userId, "DEPOSIT", amount, 0);
      User freshUser = userDao.getById(String.valueOf(userId));
      return new Response(request.getRequestId(), Response.OK, "success", freshUser);
    }
    return new Response(request.getRequestId(), Response.ERROR, "fail", null);
  }
}