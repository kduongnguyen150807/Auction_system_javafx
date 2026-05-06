package com.auction.server;

import com.auction.server.context.DaoContext;
import com.auction.server.controller.SocketServer;
import com.auction.server.dao.DatabaseMigration;
import com.auction.server.dao.LotDao;
import com.auction.server.dao.UserDao;

public class Main {
  public static void main(String[] args) {
    DatabaseMigration.runAll();

    UserDao userDao = new UserDao();
    LotDao lotDao = new LotDao();

    DaoContext daoContext = new DaoContext();
    daoContext.injectDao(userDao);
    daoContext.injectDao(lotDao);

    SocketServer server = new SocketServer(8080, daoContext);
    server.start();
  }
}
