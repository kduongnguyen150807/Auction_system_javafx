package com.auction.server.handler;

import com.auction.server.dao.UserDao;
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

    UserDao userDao = new UserDao();
    if (userDao.atomicCreditBalance(userId, amount)) {
      context.getLogDao().insertLog(userId, "DEPOSIT", amount, 0);
      User freshUser = userDao.getById(String.valueOf(userId));
      return new Response(request.getRequestId(), Response.OK, "success", freshUser);
    }
    return new Response(request.getRequestId(), Response.ERROR, "fail", null);
  }
}
