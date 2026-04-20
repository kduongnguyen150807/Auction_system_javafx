package com.auction.server.handler;

import com.auction.shared.Request;
import com.auction.shared.Response;

public interface ActionHandler {
  Response handle(Request request, HandlerContext context);
}
