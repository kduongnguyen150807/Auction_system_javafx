package com.auction.server;

import com.auction.server.dao.LotDao;
import com.auction.server.dao.UserDao;
import com.auction.server.handler.LoginHandler;
import com.auction.server.handler.RegisterLotHandler;
import com.auction.server.service.AuctionService;
import com.auction.server.service.AuthService;
import com.auction.server.utils.RequestDispatcher;
import com.auction.shared.linkv2.RequestType;

public class ServerBootstrap {
  private final RequestDispatcher requestDispatcher = new RequestDispatcher();

  public void init() {
    /* Dao */
    UserDao userDao = new UserDao();
    LotDao lotDao = new LotDao();

    /* Service */
    AuthService authService = new AuthService(userDao);
    AuctionService auctionService = new AuctionService(lotDao);

    /* Handler */
    requestDispatcher.register(RequestType.LOGIN, new LoginHandler(authService));
    requestDispatcher.register(RequestType.REGISTER_LOT, new RegisterLotHandler(auctionService));
  }

  public RequestDispatcher getRequestDispatcher() {
    return requestDispatcher;
  }
}
