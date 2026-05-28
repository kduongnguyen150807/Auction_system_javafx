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
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
public class MiscHandler implements ActionHandler {
  private static final Logger LOGGER = Logger.getLogger(MiscHandler.class.getName());

  // Nếu lớp Request chưa có hằng LOGOUT thì dùng hằng nội bộ để tránh magic string rải rác.
  private static final String ACTION_LOGOUT = "LOGOUT";

  private final UserDao userDao;
  private final BidDao bidDao;

  public MiscHandler() {
    this(new UserDao(), new BidDao());
  }

  public MiscHandler(UserDao userDao, BidDao bidDao) {
    this.userDao = Objects.requireNonNull(userDao, "userDao must not be null");
    this.bidDao = Objects.requireNonNull(bidDao, "bidDao must not be null");
  }

  @Override
  public Response handle(Request request, HandlerContext context) {
    if (request == null) {
      LOGGER.warning("Received null request");
      return new Response(null, Response.ERROR, "null_request", null);
    }

    String requestId = request.getRequestId();
    String action = request.getAction();

    try {
      MiscAction miscAction = MiscAction.from(action);
      LOGGER.info(() -> "Handling misc action: " + action + ", requestId=" + requestId);

      switch (miscAction) {
        case LOGOUT:
          return handleLogout(requestId, context);

        case REFRESH_USER:
          return handleRefreshUser(requestId, request.getPayload());

        case GET_TRANSACTIONS:
          if (!isAuthenticated(context)) {
            return error(requestId, "unauthorized");
          }
          return handleGetTransactions(requestId, request.getPayload(), context);

        case GET_STATUS_STATS:
          if (!isAuthenticated(context)) {
            return error(requestId, "unauthorized");
          }
          return ok(requestId, "success", (Serializable) context.getItemDao().getStatusStats());

        case GET_CATEGORY_STATS:
          if (!isAuthenticated(context)) {
            return error(requestId, "unauthorized");
          }
          return ok(requestId, "success", (Serializable) context.getItemDao().getCategoryStats());

        case GET_BID_HISTORY:
          if (!isAuthenticated(context)) {
            return error(requestId, "unauthorized");
          }
          return handleGetBidHistory(requestId, request.getPayload());

        case PING:
          return ok(requestId, "pong", null);

        default:
          return error(requestId, "unknown_action");
      }
    } catch (IllegalArgumentException e) {
      LOGGER.warning(() -> "Unknown action: " + action + ", requestId=" + requestId);
      return error(requestId, "unknown_action");
    } catch (ClassCastException e) {
      LOGGER.log(Level.WARNING, "Invalid payload type, requestId=" + requestId, e);
      return error(requestId, "invalid_payload_type");
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Server error while handling requestId=" + requestId, e);
      return error(requestId, "server_error");
    }
  }

  private Response handleLogout(String requestId, HandlerContext context) {
    if (context != null) {
      context.setCurrentUser(null);
    }
    LOGGER.info(() -> "User logged out, requestId=" + requestId);
    return ok(requestId, "logged_out", null);
  }

  private Response handleRefreshUser(String requestId, Object payload) {
    if (payload == null) {
      return error(requestId, "missing_user_id");
    }

    User user = userDao.getById(String.valueOf(payload));
    if (user == null) {
      return error(requestId, "user_not_found");
    }

    return ok(requestId, "success", user);
  }

  private Response handleGetTransactions(String requestId, Object payload, HandlerContext context) {
    int userId = requireIntPayload(payload);
    List<TransactionLog> logs = context.getLogDao().getByUserId(userId);
    return ok(requestId, "success", (Serializable) logs);
  }

  private Response handleGetBidHistory(String requestId, Object payload) {
    int itemId = requireIntPayload(payload);
    List<BidTransaction> history = bidDao.getBidHistory(itemId);
    return ok(requestId, "success", (Serializable) history);
  }

  private int requireIntPayload(Object payload) {
    if (!(payload instanceof Integer)) {
      throw new ClassCastException("Payload must be Integer but was "
              + (payload == null ? "null" : payload.getClass().getName()));
    }
    return (Integer) payload;
  }

  private boolean isAuthenticated(HandlerContext context) {
    return context != null && context.getCurrentUser() != null;
  }

  private Response ok(String requestId, String message, Serializable payload) {
    return new Response(requestId, Response.OK, message, payload);
  }

  private Response error(String requestId, String message) {
    return new Response(requestId, Response.ERROR, message, null);
  }

  private enum MiscAction {
    LOGOUT(ACTION_LOGOUT),
    REFRESH_USER(Request.REFRESH_USER),
    GET_TRANSACTIONS(Request.GET_TRANSACTIONS),
    GET_STATUS_STATS(Request.GET_STATUS_STATS),
    GET_CATEGORY_STATS(Request.GET_CATEGORY_STATS),
    GET_BID_HISTORY(Request.GET_BID_HISTORY),
    PING(Request.PING);

    private final String value;

    MiscAction(String value) {
      this.value = value;
    }

    static MiscAction from(String action) {
      for (MiscAction miscAction : values()) {
        if (miscAction.value.equals(action)) {
          return miscAction;
        }
      }
      throw new IllegalArgumentException("Unknown action: " + action);
    }
  }
}