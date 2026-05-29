package com.auction.server.handler.user;

import com.auction.server.dao.user.UserDao;
import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.Map;

public class DepositHandler implements ActionHandler {
  private static final double MAX_DEPOSIT_AMOUNT = 100_000_000D;

  @Override
  public Response handle(Request request, HandlerContext context) {
    User currentUser = context.getCurrentUser();
    if (currentUser == null) {
      return new Response(request.getRequestId(), Response.ERROR, "not_logged_in", null);
    }

    double amount;
    try {
      Map<String, ?> data = (Map<String, ?>) request.getPayload();
      amount = parseDouble(data.get("amount"));
    } catch (Exception e) {
      return new Response(request.getRequestId(), Response.ERROR, "Số tiền nạp không hợp lệ!", null);
    }

    // Không lấy userid từ client để tránh user tự sửa payload nạp tiền cho tài khoản khác.
    int userId = currentUser.getId();

    if (amount <= 0 || amount > MAX_DEPOSIT_AMOUNT) {
      return new Response(
              request.getRequestId(),
              Response.ERROR,
              "Số tiền nạp phải lớn hơn 0 và không vượt quá " + MAX_DEPOSIT_AMOUNT + "!",
              null);
    }

    UserDao userDao = new UserDao();
    if (userDao.atomicCreditBalance(userId, amount)) {
      context.getLogDao().insertLog(userId, "DEPOSIT", amount, 0);
      User freshUser = userDao.getById(String.valueOf(userId));
      if (freshUser != null) freshUser.setPassword("");
      return new Response(request.getRequestId(), Response.OK, "success", freshUser);
    }

    return new Response(request.getRequestId(), Response.ERROR, "fail", null);
  }

  private double parseDouble(Object value) {
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    return Double.parseDouble(String.valueOf(value));
  }
}