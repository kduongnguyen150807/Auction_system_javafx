package com.auction.server.handler.spin;

import com.auction.server.dao.user.UserDao;
import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.service.spin.SpinWheelService;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.SpinWheelResult;
import com.auction.shared.SpinWheelState;
import com.auction.shared.User;
import java.util.Map;

public class SpinWheelHandler implements ActionHandler {
  private final SpinWheelService spinService = SpinWheelService.getInstance();
  private final UserDao userDao = new UserDao();

  @Override
  public Response handle(Request request, HandlerContext context) {
    User current = context.getCurrentUser();
    String action = request.getAction();
    String requestId = request.getRequestId();

    if (current == null) {
      return new Response(requestId, Response.ERROR, "Unauthorized", null);
    }

    return switch (action) {
      case Request.GET_SPIN_WHEEL_STATE -> {
        SpinWheelState state = spinService.buildState(current);
        yield new Response(requestId, Response.OK, "success", state);
      }
      case Request.SPIN_WHEEL -> {
        SpinWheelResult result = spinService.spin(current, userDao);
        if (result.getUser() == null) {
          yield new Response(requestId, Response.ERROR, result.getMessage(), result);
        }
        context.getLogDao().insertLog(current.getId(), "SPIN_WHEEL", 0, 0);
        context.setCurrentUser(result.getUser());
        yield new Response(requestId, Response.OK, result.getMessage(), result);
      }
      case Request.BUY_SPIN_CREDITS -> {
        int count = parseCount(request.getPayload());
        if (count <= 0 || count > 50) {
          yield new Response(requestId, Response.ERROR, "Số lượt mua không hợp lệ (1–50)", null);
        }
        User fresh = spinService.buyCredits(current, count, userDao);
        if (fresh == null) {
          yield new Response(requestId, Response.ERROR, "Số dư không đủ hoặc giao dịch thất bại", null);
        }
        context.getLogDao().insertLog(current.getId(), "BUY_SPIN", count * SpinWheelState.SPIN_CREDIT_PRICE, 0);
        context.setCurrentUser(fresh);
        yield new Response(requestId, Response.OK, "success", spinService.buildState(fresh));
      }
      default -> new Response(requestId, Response.ERROR, "unknown_spin_action", null);
    };
  }

  private static int parseCount(Object payload) {
    if (payload instanceof Number n) {
      return n.intValue();
    }
    if (payload instanceof Map<?, ?> map) {
      Object raw = map.get("count");
      if (raw instanceof Number num) {
        return num.intValue();
      }
    }
    return 0;
  }
}
