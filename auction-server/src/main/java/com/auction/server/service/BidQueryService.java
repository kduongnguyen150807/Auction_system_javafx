package com.auction.server.service;

import com.auction.server.dao.BidDao;
import com.auction.shared.BidTransaction;
import java.util.List;

public class BidQueryService {
    private final BidDao bidDao;

    public BidQueryService() {
        this.bidDao = new BidDao();
    }

    public List<BidTransaction> getBidHistory(int itemId) {
        return bidDao.getByItem(itemId);
    }
}