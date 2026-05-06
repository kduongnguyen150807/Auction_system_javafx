package com.auction.server.context;

import com.auction.server.controller.ClientHandler;
import com.auction.shared.user.User;

public class HandlerContext {
  private final DaoContext daoContext;
  private final ClientHandler clientHandler;
  private User user;

  public HandlerContext(DaoContext daoContext, ClientHandler clientHandler) {
    this.daoContext = daoContext;
    this.clientHandler = clientHandler;
  }

  public DaoContext getDaoContext() {
    return daoContext;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
