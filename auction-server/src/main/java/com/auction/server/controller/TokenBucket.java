package com.auction.server.controller;

public class TokenBucket {
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
        long diff = now - lastrefill;
        if (diff > 100) {
            int add = (int) (diff / 100) * 10;
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