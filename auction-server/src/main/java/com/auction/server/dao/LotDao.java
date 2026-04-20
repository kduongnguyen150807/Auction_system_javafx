package com.auction.server.dao;

import com.auction.shared.Lot;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LotDao implements LotRepository {
  private static final Logger LOGGER = Logger.getLogger(LotDao.class.getName());

  private Connection getConn() {
    return DatabaseConnection.getInstance().getConnection();
  }

  @Override
  public List<Lot> getOngoingBids(int userId) {
    return queryLots("SELECT i.*, u.username as s_name, u.avatar_url as s_avatar "
        + "FROM items i LEFT JOIN users u ON i.sellerid = u.id "
        + "WHERE i.status = 'OPEN' AND i.starttime <= NOW() AND i.endtime > NOW()");
  }

  @Override
  public List<Lot> getUpcomingBids(int userId) {
    return queryLots("SELECT i.*, u.username as s_name, u.avatar_url as s_avatar "
        + "FROM items i LEFT JOIN users u ON i.sellerid = u.id "
        + "WHERE i.status = 'OPEN' AND i.starttime > NOW()");
  }

  @Override
  public List<Lot> getClosedBids(int userId) {
    return queryLots("SELECT i.*, u.username as s_name, u.avatar_url as s_avatar, w.username as w_name "
        + "FROM items i LEFT JOIN users u ON i.sellerid = u.id "
        + "LEFT JOIN users w ON i.winnerid = w.id "
        + "WHERE i.status = 'CLOSED'");
  }

  @Override
  public List<Lot> getPastBids(int userId) {
    return queryLots("SELECT i.*, u.username as s_name, u.avatar_url as s_avatar, w.username as w_name "
        + "FROM items i LEFT JOIN users u ON i.sellerid = u.id "
        + "LEFT JOIN users w ON i.winnerid = w.id "
        + "WHERE i.status IN ('FINISHED', 'CANCELED') OR (i.status = 'OPEN' AND i.endtime <= NOW())");
  }

  private List<Lot> queryLots(String sql) {
    List<Lot> lots = new ArrayList<>();
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ResultSet rs = ps.executeQuery();
      while (rs.next()) lots.add(mapResultSetToLot(rs));
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Lot query failed", e);
    }
    return lots;
  }

  private Lot mapResultSetToLot(ResultSet rs) throws SQLException {
    Lot lot = new Lot();
    lot.setId(rs.getInt("id"));
    lot.setTitle(rs.getString("name"));
    lot.setDescription(rs.getString("description"));
    lot.setBidValue(rs.getDouble("currentprice"));
    lot.setStartTime(rs.getTimestamp("starttime").toLocalDateTime());
    lot.setEndTime(rs.getTimestamp("endtime").toLocalDateTime());
    lot.setImageUrl(rs.getString("image_url"));
    lot.setSellerUsername(rs.getString("s_name"));
    lot.setSellerAvatarUrl(rs.getString("s_avatar"));
    try {
      String winnerName = rs.getString("w_name");
      if (winnerName != null) lot.setWinnerUsername(winnerName);
    } catch (Exception e) { /* w_name may not exist */ }
    return lot;
  }
}
