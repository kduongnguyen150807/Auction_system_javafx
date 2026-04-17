package com.auction.shared;

import java.sql.Timestamp;

public class BidFactory {
    public static BidTransaction createBid(Double bidvalue, Timestamp timestamp) {
        BidTransaction bid = new BidTransaction();
        bid.setBidValue(bidvalue);
        bid.setTimestamp(timestamp.toLocalDateTime());
        return bid;
    }
}
