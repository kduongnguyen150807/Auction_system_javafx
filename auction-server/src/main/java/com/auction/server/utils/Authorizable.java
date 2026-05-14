package com.auction.server.utils;

import com.auction.server.context.HandlerContext;
import com.auction.shared.linkv2.Request;

public interface Authorizable<T> {
  boolean authorize (HandlerContext handlerContext, Request<T> request);
}
