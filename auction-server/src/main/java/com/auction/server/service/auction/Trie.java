package com.auction.server.service.auction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trie {
    // REFACTOR: consider extracting trienode into a separate top-level class if it grows complex
    private static class trienode {
        boolean isword;
        Map<Character, trienode> children = new HashMap<>();
    }

    private final trienode root = new trienode();

    public void insert(String word) {
        trienode curr = root;
        for (char c : word.toLowerCase().toCharArray()) {
            curr.children.putIfAbsent(c, new trienode());
            curr = curr.children.get(c);
        }
        curr.isword = true;
    }

    public List<String> search(String prefix) {
        List<String> ans = new ArrayList<>();
        trienode curr = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            curr = curr.children.get(c);
            if (curr == null) {
                return ans;
            }
        }
        dfs(curr, prefix.toLowerCase(), ans);
        return ans;
    }

    // REFACTOR: limit the size of 'ans' to prevent memory overflow on short prefixes
    private void dfs(trienode node, String path, List<String> ans) {
        if (node.isword) {
            ans.add(path);
        }
        for (Map.Entry<Character, trienode> entry : node.children.entrySet()) {
            dfs(entry.getValue(), path + entry.getKey(), ans);
        }
    }
}