package com.auction.server.dao;

import com.auction.shared.Item;
import com.auction.shared.ItemFactory;
import com.auction.shared.ItemStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queries the items table enriched with seller/winner display fields.
 * Returns {@link Item} objects directly — no intermediate {@code Lot} DTO
 * required.
 */
public class LotDao implements LotRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(LotDao.class);

  private Connection getConn() {
    return DatabaseConnection.getInstance().getConnection();
  }

  @Override
  public List<Item> getOngoingBids(int userId) {
    return queryItems(
        "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar "
            + "FROM items i LEFT JOIN users u ON i.sellerid = u.id "
            + "WHERE i.status = 'OPEN' AND i.starttime <= NOW() AND i.endtime > NOW()");
  }

  @Override
  public List<Item> getUpcomingBids(int userId) {
    return queryItems(
        "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar "
            + "FROM items i LEFT JOIN users u ON i.sellerid = u.id "
            + "WHERE i.status = 'OPEN' AND i.starttime > NOW()");
  }

  @Override
  public List<Item> getClosedBids(int userId) {
    return queryItems(
        "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar, w.username as w_name "
            + "FROM items i LEFT JOIN users u ON i.sellerid = u.id "
            + "LEFT JOIN users w ON i.winnerid = w.id "
            + "WHERE i.status = 'CLOSED'");
  }

  @Override
  public List<Item> getPastBids(int userId) {
    return queryItems(
        "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar, w.username as w_name "
            + "FROM items i LEFT JOIN users u ON i.sellerid = u.id "
            + "LEFT JOIN users w ON i.winnerid = w.id "
            + "WHERE i.status IN ('FINISHED', 'CANCELED') "
            + "   OR (i.status = 'OPEN' AND i.endtime <= NOW())");
  }

  private List<Item> queryItems(String sql) {
    List<Item> items = new ArrayList<>();
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ResultSet rs = ps.executeQuery();
      while (rs.next()) items.add(mapResultSetToItem(rs));
    } catch (Exception e) {
      LOGGER.warn("Lot query failed", e);
    }
    return items;
  }

  private Item mapResultSetToItem(ResultSet rs) throws SQLException {
    String category = rs.getString("category");
    Item item = ItemFactory.createItem(category);

    item.setId(rs.getInt("id"));
    item.setName(rs.getString("name"));
    item.setDescription(rs.getString("description"));
    item.setCurrentPrice(rs.getDouble("currentprice"));
    item.setStartingPrice(rs.getDouble("startingprice"));
    item.setMaxPrice(rs.getDouble("maxprice"));
    item.setSellerId(rs.getInt("sellerid"));
    item.setWinnerId(rs.getInt("winnerid"));
    item.setImageUrl(rs.getString("image_url"));
    item.setSellerUsername(rs.getString("s_name"));
    item.setSellerAvatarUrl(rs.getString("s_avatar"));
    Timestamp start = rs.getTimestamp("starttime");
    if (start != null) item.setStartTime(start.toLocalDateTime());
    Timestamp end = rs.getTimestamp("endtime");
    if (end != null) item.setEndTime(end.toLocalDateTime());
    try {
      item.setStatus(ItemStatus.valueOf(rs.getString("status")));
    } catch (Exception ignored) {
      item.setStatus(ItemStatus.OPEN);
    }
    try {
      String winnerName = rs.getString("w_name");
      if (winnerName != null) item.setWinnerUsername(winnerName);
    } catch (Exception ignored) { /* w_name may not exist in some queries */ }
    return item;
  }
}
