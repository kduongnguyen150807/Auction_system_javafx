package com.auction.server.handler;

import com.auction.server.controller.ClientHandler;
import com.auction.server.dao.*;
import com.auction.server.service.AuctionManager;
import com.auction.server.service.UserService;
import com.auction.shared.User;

public class HandlerContext {
  private final UserService userService;
  private final ItemDao itemDao;
  private final LotDao lotDao;
  private final TransactionLogDao logDao;
  private final RatingDao ratingDao;
  private final ClientHandler sender;
  private User currentUser;

  public HandlerContext(UserService userService, ItemDao itemDao, LotDao lotDao,
                        TransactionLogDao logDao, RatingDao ratingDao, ClientHandler sender) {
    this.userService = userService;
    this.itemDao = itemDao;
    this.lotDao = lotDao;
    this.logDao = logDao;
    this.ratingDao = ratingDao;
    this.sender = sender;
  }

  public UserService getUserService() {
    return this.userService;
  }

  public ItemDao getItemDao() {
    return this.itemDao;
  }

  public LotDao getLotDao() {
    return this.lotDao;
  }

  public TransactionLogDao getLogDao() {
    return this.logDao;
  }

  public RatingDao getRatingDao() {
    return this.ratingDao;
  }

  public ClientHandler getSender() {
    return this.sender;
  }

  public User getCurrentUser() {
    return this.currentUser;
  }

  public void setCurrentUser(User user) {
    this.currentUser = user;
  }

  public AuctionManager getAuctionManager() {
    return AuctionManager.getInstance();
  }
}
