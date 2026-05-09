package com.auction.server.service;

import com.auction.server.dao.LotDao;
import com.auction.shared.Lot;
import java.util.List;

public class LotService {
    private final LotDao lotDao;

    public LotService() {
        this(new LotDao());
    }

    public LotService(LotDao lotDao) {
        this.lotDao = lotDao;
    }

    public List<Lot> getOngoingBids(int userId) {
        return lotDao.getOngoingBids(userId);
    }

    public List<Lot> getUpcomingBids(int userId) {
        return lotDao.getUpcomingBids(userId);
    }

    public List<Lot> getClosedBids(int userId) {
        return lotDao.getClosedBids(userId);
    }

    public List<Lot> getPastBids(int userId) {
        return lotDao.getPastBids(userId);
    }
}