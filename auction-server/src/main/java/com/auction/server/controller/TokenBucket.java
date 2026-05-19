package com.auction.server.controller;

public class TokenBucket {
    private static final long REFILL_INTERVAL_MS = 100;
    private static final int TOKENS_PER_REFILL = 10;

    private final int maxTokens;
    private int tokens;
    private long lastRefillTime;

    public TokenBucket(int maxTokens) {
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens must be greater than 0");
        }

        this.maxTokens = maxTokens;
        this.tokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
    }

    public synchronized boolean tryConsume() {
        refillIfNeeded();

        if (tokens <= 0) {
            return false;
        }

        tokens--;
        return true;
    }

    @Deprecated
    public synchronized boolean tryconsume() {
        return tryConsume();
    }

    private void refillIfNeeded() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTime;

        if (elapsed < REFILL_INTERVAL_MS) {
            return;
        }

        int refillCount = (int) (elapsed / REFILL_INTERVAL_MS);
        int tokensToAdd = refillCount * TOKENS_PER_REFILL;

        tokens = Math.min(maxTokens, tokens + tokensToAdd);
        lastRefillTime += refillCount * REFILL_INTERVAL_MS;
    }
}