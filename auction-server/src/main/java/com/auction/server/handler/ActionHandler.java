package com.auction.server.handler;

import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import com.auction.shared.UserRole;

public interface ActionHandler {
  Response handle(Request request, HandlerContext context);

  static ActionHandler requireAuth(ActionHandler inner) {
    return (req, ctx) -> {
      if (ctx.getCurrentUser() == null)
        return new Response(req.getRequestId(), Response.ERROR, "not_logged_in", null);
      return inner.handle(req, ctx);
    };
  }

  static ActionHandler requireAdmin(ActionHandler inner) {
    return (req, ctx) -> {
      User u = ctx.getCurrentUser();
      if (u == null || u.getRole() != UserRole.ADMIN)
        return new Response(req.getRequestId(), Response.ERROR, "forbidden", null);
      return inner.handle(req, ctx);
    };
  }
}
