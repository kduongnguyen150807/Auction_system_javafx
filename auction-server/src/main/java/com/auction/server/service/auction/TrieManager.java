package com.auction.server.service.auction;

import com.auction.server.dao.auction.ItemDao;
import com.auction.shared.Item;
import java.util.List;

public class TrieManager {
    // REFACTOR: implement a mechanism to dynamically update the trie when new items are added
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
        TrieManager ans = instance;
        return ans;
    }

    public List<String> search(String prefix) {
        List<String> res = trie.search(prefix);
        return res;
    }
}