package com.auction.server.repository;

import com.auction.server.dao.LotDao;
import com.auction.shared.item.Item;

import java.sql.Connection;

public class LotRepository {
  private final LotDao lotDao;

  public LotRepository(LotDao lotDao) {
    this.lotDao = lotDao;
  }

  public boolean registerLot(Item item, int sellerId, Connection connection) {
    return lotDao.registerLot(item, sellerId, connection);
  }
}
