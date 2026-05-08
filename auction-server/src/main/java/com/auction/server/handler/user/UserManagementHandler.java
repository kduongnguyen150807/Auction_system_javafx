package com.auction.server.handler.user;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;

import com.auction.server.dao.user.UserDao;
import com.auction.server.service.auction.AuctionManager;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import com.auction.shared.UserRole;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserManagementHandler implements ActionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementHandler.class);

  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String requestId = request.getRequestId();
    Object payload = request.getPayload();

    switch (action) {
      case Request.GET_ALL_USERS:
        return new Response(
            requestId, Response.OK, "success", context.getUserService().getAllUsers());

      case Request.LOCK_USER:
      case Request.UNLOCK_USER:
        return handleLockUser(action, requestId, payload, context);

      case Request.PROMOTE_ADMIN: {
        String data = (String) payload;
        String[] parts = data.split(":");
        String username = parts[0];
        String role = parts.length > 1 ? parts[1] : UserRole.ADMIN.name();
        boolean success = context.getUserService().setUserRole(username, role);
        return new Response(
            requestId, success ? Response.OK : Response.ERROR, success ? "success" : "fail", null);
      }

      case Request.SEARCH_USERS: {
        List<User> users = new UserDao().searchUsers((String) payload);
        return new Response(requestId, Response.OK, "success", (java.io.Serializable) users);
      }

      case Request.GET_USER_BY_ID: {
        int userId = (int) payload;
        User user = new UserDao().getById(String.valueOf(userId));
        if (user != null) {
          user.setPassword("");
        }
        return new Response(
            requestId,
            user != null ? Response.OK : Response.ERROR,
            user != null ? "success" : "not_found",
            user);
      }

      default:
        return new Response(requestId, Response.ERROR, "unknown_action", null);
    }
  }

  /**
   * Handles both LOCK_USER and UNLOCK_USER. When a user is being LOCKED we
   * cascade ban-handling into {@link AuctionManager} so any open auction the
   * user is involved in is reconciled (bids invalidated for a banned bidder,
   * auctions cancelled and escrow refunded for a banned seller).
   *
   * <p>The auction reconciliation runs <em>after</em> the user row is flipped
   * to {@code islocked = true} so any in-flight bid that grabs the auction
   * lock first will see the locked flag during validation and self-reject.
   */
  private Response handleLockUser(
      String action, String requestId, Object payload, HandlerContext context) {
    String username = (String) payload;
    boolean lock = action.equals(Request.LOCK_USER);

    UserDao userDao = new UserDao();
    User target = userDao.getByUsername(username);
    if (target == null) {
      return new Response(requestId, Response.ERROR, "user_not_found", null);
    }

    boolean success = context.getUserService().setUserLocked(username, lock);
    if (!success) {
      return new Response(requestId, Response.ERROR, "fail", null);
    }

    AuctionManager auctionManager = AuctionManager.getInstance();
    if (lock) {
      try {
        auctionManager.handleBidderBan(target.getId());
        auctionManager.handleSellerBan(target.getId());
      } catch (Exception e) {
        LOGGER.error(
            "Ban cascade failed for user={} role={}", target.getId(), target.getRole(), e);
      }
      // Push a real-time notification to the banned user so they are
      // immediately force-logged out and informed on their client.
      auctionManager.sendToUser(
          target.getId(),
          new Response("", Response.ACCOUNT_BANNED, "Your account has been suspended by an administrator.", null));
      LOGGER.info("Sent ACCOUNT_BANNED push to user={}", target.getId());
    } else {
      // Notify the user that their account has been reinstated.
      auctionManager.sendToUser(
          target.getId(),
          new Response("", Response.ACCOUNT_UNBANNED, "Your account has been reinstated.", null));
      LOGGER.info("Sent ACCOUNT_UNBANNED push to user={}", target.getId());
    }

    return new Response(requestId, Response.OK, "success", null);
  }
}
