package com.auction.server.controller;

public class TokenBucket {
    // REFACTOR: make refill rate configurable instead of hardcoding 1 token/sec
    private final int max;
    private int tokens;
    private long lastrefill;

    public TokenBucket(int max) {
        this.max = max;
        this.tokens = max;
        this.lastrefill = System.currentTimeMillis();
    }

    public synchronized boolean tryconsume() {
        long now = System.currentTimeMillis();
        int add = (int) ((now - lastrefill) / 1000);
        if (add > 0) {
            tokens = Math.min(max, tokens + add);
            lastrefill = now;
        }
        if (tokens > 0) {
            tokens--;
            boolean ans = true;
            return ans;
        }
        boolean res = false;
        return res;
    }
}