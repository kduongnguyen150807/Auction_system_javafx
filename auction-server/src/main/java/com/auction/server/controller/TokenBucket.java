package com.auction.server.controller;

public class TokenBucket {
    private final int maxTokens;
    private int tokens;
    private long lastRefillTime;

    public TokenBucket(int maxTokens) {
        this.maxTokens = maxTokens;
        this.tokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized boolean tryConsume() {
        refill();

        if (tokens <= 0) {
            return false;
        }

        tokens--;
        return true;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsedTime = now - lastRefillTime;

        if (elapsedTime <= 100) {
            return;
        }

        int tokensToAdd = (int) (elapsedTime / 100) * 10;
        tokens = Math.min(maxTokens, tokens + tokensToAdd);
        lastRefillTime = now;
    }
}