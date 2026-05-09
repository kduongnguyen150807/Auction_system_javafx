package com.auction.server.handler.dispatch;

import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.HashMap;
import java.util.Map;

public class ActionRegistry {
  private final Map<String, ActionHandler> handlers = new HashMap<>();

  public void register(String action, ActionHandler handler) {
    this.handlers.put(action, handler);
  }

  public Response dispatch(Request request, HandlerContext context) {
    ActionHandler handler = this.handlers.get(request.getAction());
    if (handler == null) {
      return new Response(request.getRequestId(), Response.ERROR, "unknown_action", null);
    }
    return handler.handle(request, context);
  }
}
