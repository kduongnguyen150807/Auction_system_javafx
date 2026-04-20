package com.auction.server.handler;

import com.auction.server.dao.UserDao;
import com.auction.shared.*;
import java.util.List;

public class UserManagementHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String requestId = request.getRequestId();
    Object payload = request.getPayload();

    switch (action) {
      case Request.GET_ALL_USERS:
        return new Response(requestId, Response.OK, "success", context.getUserService().getAllUsers());

      case Request.LOCK_USER:
      case Request.UNLOCK_USER: {
        boolean lock = action.equals(Request.LOCK_USER);
        boolean success = context.getUserService().setUserLocked((String) payload, lock);
        return new Response(requestId,
            success ? Response.OK : Response.ERROR,
            success ? "success" : "fail", null);
      }

      case Request.PROMOTE_ADMIN: {
        if (context.getCurrentUser() == null || context.getCurrentUser().getRole() != UserRole.ADMIN) {
          return new Response(requestId, Response.ERROR, "forbidden", null);
        }
        String data = (String) payload;
        String[] parts = data.split(":");
        String username = parts[0];
        String role = parts.length > 1 ? parts[1] : UserRole.ADMIN.name();
        boolean success = context.getUserService().setUserRole(username, role);
        return new Response(requestId,
            success ? Response.OK : Response.ERROR,
            success ? "success" : "fail", null);
      }

      case Request.SEARCH_USERS: {
        List<User> users = new UserDao().searchUsers((String) payload);
        return new Response(requestId, Response.OK, "success", (java.io.Serializable) users);
      }

      case Request.GET_USER_BY_ID: {
        int userId = (int) payload;
        User user = new UserDao().getById(String.valueOf(userId));
        if (user != null) user.setPassword("");
        return new Response(requestId,
            user != null ? Response.OK : Response.ERROR,
            user != null ? "success" : "not_found", user);
      }

      default:
        return new Response(requestId, Response.ERROR, "unknown_action", null);
    }
  }
}
