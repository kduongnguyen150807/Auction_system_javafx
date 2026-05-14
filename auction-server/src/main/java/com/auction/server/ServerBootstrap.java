package com.auction.server;

import com.auction.server.dao.ItemDao;
import com.auction.server.dao.LotDao;
import com.auction.server.dao.UserDao;
import com.auction.server.handler.*;
import com.auction.server.handler.auth.LoginHandler;
import com.auction.server.handler.auth.RegisterHandler;
import com.auction.server.repository.ItemRepository;
import com.auction.server.repository.LotRepository;
import com.auction.server.repository.UserRepository;
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
    ItemDao itemDao = new ItemDao();

    /* Repository */
    UserRepository userRepository = new UserRepository(userDao);
    ItemRepository auctionRepository = new ItemRepository(itemDao);
    LotRepository lotRepository = new LotRepository(lotDao);

    /* Service */
    AuthService authService = new AuthService(userRepository);
    AuctionService auctionService = new AuctionService(auctionRepository,  userRepository, lotRepository);

    /* Handler */
    requestDispatcher.register(RequestType.LOGIN, new LoginHandler(authService));
    requestDispatcher.register(RequestType.REGISTER_LOT, new RegisterLotHandler(auctionService));
    requestDispatcher.register(RequestType.GET_ALL_ITEMS, new GetAllItemsHandler(auctionService));
    requestDispatcher.register(RequestType.APPROVE_ITEM, new ApproveItemHandler(auctionService));
    requestDispatcher.register(RequestType.PLACE_BID, new PlaceBidHandler(auctionService));
    requestDispatcher.register(RequestType.REGISTER, new RegisterHandler(authService));
  }

  public RequestDispatcher getRequestDispatcher() {
    return requestDispatcher;
  }
}
