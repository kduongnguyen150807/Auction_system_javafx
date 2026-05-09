package com.auction.server.service;

import com.auction.server.dao.BidDao;
import com.auction.shared.BidTransaction;
import java.util.List;

public class BidQueryService {
    private final BidDao bidDao;

    public BidQueryService() {
        this(new BidDao());
    }

    public BidQueryService(BidDao bidDao) {
        this.bidDao = bidDao;
    }

    public List<BidTransaction> getBidHistory(int itemId) {
        return bidDao.getByItem(itemId);
    }
}