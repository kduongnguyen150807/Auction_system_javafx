package com.auction.server.service;

import com.auction.server.dao.ItemDao;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import java.util.List;
import java.util.Map;

public class ItemQueryService {
    private final ItemDao itemDao;

    public ItemQueryService() {
        this(new ItemDao());
    }

    public ItemQueryService(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    public List<Item> getOpenItems() {
        List<Item> items = itemDao.getAll();
        items.removeIf(item -> item.getStatus() != ItemStatus.OPEN);
        return items;
    }

    public Item getById(int itemId) {
        return itemDao.getById(itemId);
    }

    public List<Item> getBySellerId(int sellerId) {
        return itemDao.getBySellerId(sellerId);
    }

    public List<Item> getPendingItems() {
        return itemDao.getPendingItems();
    }

    public boolean approveItem(int itemId) {
        return itemDao.approveItem(itemId);
    }

    public boolean rejectItem(int itemId) {
        return itemDao.rejectItem(itemId);
    }

    public Map<String, Integer> getStatusStats() {
        return itemDao.getStatusStats();
    }

    public Map<String, Double> getCategoryStats() {
        return itemDao.getCategoryStats();
    }
}