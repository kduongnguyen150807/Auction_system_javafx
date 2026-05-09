package com.auction.server.context;

import com.auction.server.controller.ClientHandler;
import com.auction.shared.user.User;

public class HandlerContext {
  private final ClientHandler clientHandler;
  private User user;

  public HandlerContext(ClientHandler clientHandler) {
    this.clientHandler = clientHandler;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
