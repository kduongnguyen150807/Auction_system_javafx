package com.auction.server.utils;

import com.auction.server.context.HandlerContext;

public interface Authorizable {
  boolean authorize (HandlerContext handlerContext);
}
