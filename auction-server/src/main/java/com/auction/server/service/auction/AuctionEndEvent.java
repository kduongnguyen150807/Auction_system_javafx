package com.auction.server.service.auction;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class AuctionEndEvent implements Delayed {
    private final int itemid;
    private final long exptime;

    public AuctionEndEvent(int itemid, LocalDateTime endtime) {
        this.itemid = itemid;
        this.exptime = endtime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public int getItemid() {
        return itemid;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long ans = exptime - System.currentTimeMillis();
        return unit.convert(ans, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        long ans = this.getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);
        return Long.compare(ans, 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AuctionEndEvent ans = (AuctionEndEvent) obj;
        return itemid == ans.itemid;
    }

    @Override
    public int hashCode() {
        return itemid;
    }
}