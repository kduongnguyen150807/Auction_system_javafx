package com.auction.server.service.auction;

import com.auction.server.dao.auction.ItemDao;
import com.auction.shared.Item;
import java.util.List;

public class TrieManager {
    private static final TrieManager instance = new TrieManager();
    private final Trie trie = new Trie();

    private TrieManager() {
        ItemDao dao = new ItemDao();
        List<Item> items = dao.getAll();
        for (Item item : items) {
            if (item.getName() != null) {
                trie.insert(item.getName());
            }
        }
    }

    public static TrieManager getInstance() {
        return instance;
    }

    public List<String> search(String prefix) {
        return trie.search(prefix);
    }

    // TÍNH NĂNG 5: DYNAMIC TRIE UPDATE
    public synchronized void insertNewItem(String itemName) {
        if (itemName != null && !itemName.trim().isEmpty()) {
            trie.insert(itemName.trim());
        }
    }
}