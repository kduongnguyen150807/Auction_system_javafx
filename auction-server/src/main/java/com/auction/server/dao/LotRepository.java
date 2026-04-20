package com.auction.server.dao;

import com.auction.shared.Lot;
import java.util.List;

public interface LotRepository {
  List<Lot> getOngoingBids(int userId);
  List<Lot> getUpcomingBids(int userId);
  List<Lot> getClosedBids(int userId);
  List<Lot> getPastBids(int userId);
}
