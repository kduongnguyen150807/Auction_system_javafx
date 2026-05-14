package com.auction.server.repository;

import com.auction.server.dao.ItemDao;
import com.auction.shared.item.Item;
import com.auction.shared.item.ItemStatus;

import java.sql.Connection;
import java.util.List;

public class ItemRepository {
  private final ItemDao itemDao;

  public ItemRepository(ItemDao itemDao) {
    this.itemDao = itemDao;
  }

  public List<Item> getAllItems(Connection connection) {
    return itemDao.getAllItems(connection);
  }

  public boolean approveItem(int itemId, Connection connection) {
    return itemDao.approveItem(itemId, connection);
  }

  public Item getItemById(int itemId,  Connection connection) {
    return itemDao.getItemById(itemId, connection);
  }

  public boolean updateBidInfo(int itemId, double bidAmount, int winnerId, Connection connection) {
    return itemDao.updateBidInfo(itemId, bidAmount, winnerId, connection);
  }

  public boolean updateStatus(int itemId, ItemStatus itemStatus, Connection connection) {
    return itemDao.updateItemStatus(itemId, itemStatus, connection);
  }

  public Item findItemToUpdate(int itemId, Connection connection) {
    return itemDao.findForUpdate(itemId, connection);
  }
}
