package com.auction.server.service;

import com.auction.server.dao.ItemDao;
import com.auction.shared.Item;
import java.util.List;
import java.util.Map;

public class ItemQueryService {
    private final ItemDao itemDao;

    public ItemQueryService() {
        this.itemDao = new ItemDao();
    }

    public List<Item> getOpenItems() {
        List<Item> items = itemDao.getAll();
        items.removeIf(i -> i.getStatus() != com.auction.shared.ItemStatus.OPEN);
        return items;
    }

    public Item getById(int id) {
        return itemDao.getById(id);
    }

    public List<Item> getBySellerId(int sellerId) {
        return itemDao.getBySellerId(sellerId);
    }

    public List<Item> getPendingItems() {
        return itemDao.getPendingItems();
    }

    public boolean approveItem(int id) {
        return itemDao.approveItem(id);
    }

    public boolean rejectItem(int id) {
        return itemDao.rejectItem(id);
    }

    public Map<String, Integer> getStatusStats() {
        return itemDao.getStatusStats();
    }

    public Map<String, Double> getCategoryStats() {
        return itemDao.getCategoryStats();
    }
}