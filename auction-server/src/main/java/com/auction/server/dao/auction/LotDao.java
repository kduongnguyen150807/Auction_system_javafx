package com.auction.server.dao.auction;

import com.auction.server.dao.platform.BaseDao;
import com.auction.server.service.auction.TrendingLotsFormula;
import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import com.auction.shared.ItemFactory;
import com.auction.shared.ItemStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queries the items table enriched with seller/winner display fields; each row is mapped directly to
 * {@link Item}.
 */
public class LotDao extends BaseDao<Item> implements LotRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(LotDao.class);

  @Override
  protected Item mapRow(ResultSet rs) throws SQLException {
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
    item.setStatus(ItemStatus.parse(rs.getString("status")));
    try {
      item.setAuctionType(AuctionType.parse(rs.getString("auction_type")));
      double dr = rs.getDouble("dutch_reserve_price");
      if (!rs.wasNull()) item.setDutchReservePrice(dr);
      double da = rs.getDouble("dutch_tick_amount");
      if (!rs.wasNull()) item.setDutchTickAmount(da);
      int im = rs.getInt("dutch_tick_interval_mins");
      if (!rs.wasNull()) item.setDutchTickIntervalMinutes(im);
    } catch (Exception ignored) {
    }
    try {
      String winnerName = rs.getString("w_name");
      if (winnerName != null) item.setWinnerUsername(winnerName);
    } catch (Exception ignored) { /* w_name may not exist in some queries */ }
    return item;
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

  @Override
  public List<Item> getTrendingLiveItems(AuctionType auctionType, int limit) {
    AuctionType kind = auctionType != null ? auctionType : AuctionType.ENGLISH;
    int lim = Math.max(1, Math.min(limit, 20));
    String typeName = kind.dbName();
    List<TrendingAggRow> agg = fetchTrendingAggregates(typeName);
    agg.sort(
        (r1, r2) ->
            Double.compare(
                TrendingLotsFormula.computeTrendScore(r2.wLong, r2.wShort, r2.distinctBidders),
                TrendingLotsFormula.computeTrendScore(r1.wLong, r1.wShort, r1.distinctBidders)));
    List<Integer> orderedIds = new ArrayList<>();
    for (TrendingAggRow row : agg) {
      if (orderedIds.size() >= lim) {
        break;
      }
      double s =
          TrendingLotsFormula.computeTrendScore(row.wLong, row.wShort, row.distinctBidders);
      if (s > TrendingLotsFormula.EPS) {
        orderedIds.add(row.itemId);
      }
    }
    if (orderedIds.size() < lim) {
      orderedIds.addAll(fetchBackfillLiveItemIds(typeName, orderedIds, lim - orderedIds.size()));
    }
    return loadLiveItemsPreservingOrder(orderedIds);
  }

  /** Pre-sort key only — sort uses full formula fields on row. */
  private List<TrendingAggRow> fetchTrendingAggregates(String auctionTypeName) {
    String sql =
        "SELECT b.itemid AS item_id, "
            + "SUM(EXP(-TIMESTAMPDIFF(MINUTE, b.timestamp, NOW()) / (60 * ?))) AS w_long, "
            + "SUM(CASE WHEN b.timestamp >= NOW() - INTERVAL ? MINUTE "
            + "THEN EXP(-TIMESTAMPDIFF(MINUTE, b.timestamp, NOW()) / (60 * ?)) ELSE 0 END) AS w_short, "
            + "COUNT(DISTINCT b.userid) AS u_cnt "
            + "FROM bid_transactions b "
            + "INNER JOIN items i ON i.id = b.itemid "
            + "WHERE i.status = 'OPEN' AND i.starttime <= NOW() AND i.endtime > NOW() "
            + "AND i.auction_type = ? "
            + "AND b.timestamp >= NOW() - INTERVAL ? HOUR "
            + "GROUP BY b.itemid";
    List<TrendingAggRow> out = new ArrayList<>();
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setDouble(1, TrendingLotsFormula.TAU_DECAY_HOURS);
      ps.setInt(2, TrendingLotsFormula.SHORT_WINDOW_MINUTES);
      ps.setDouble(3, TrendingLotsFormula.TAU_DECAY_HOURS);
      ps.setString(4, auctionTypeName);
      ps.setInt(5, TrendingLotsFormula.LONG_WINDOW_HOURS);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.add(
              new TrendingAggRow(
                  rs.getInt("item_id"),
                  rs.getDouble("w_long"),
                  rs.getDouble("w_short"),
                  rs.getInt("u_cnt")));
        }
      }
    } catch (SQLException e) {
      LOGGER.warn("fetchTrendingAggregates failed", e);
    }
    return out;
  }

  private List<Integer> fetchBackfillLiveItemIds(
      String auctionTypeName, List<Integer> excluded, int need) {
    List<Integer> out = new ArrayList<>();
    if (need <= 0) {
      return out;
    }
    StringBuilder sql =
        new StringBuilder(
            "SELECT i.id FROM items i WHERE i.status = 'OPEN' AND i.starttime <= NOW() AND i.endtime > NOW() "
                + "AND i.auction_type = ? ");
    List<Object> params = new ArrayList<>();
    params.add(auctionTypeName);
    if (excluded != null && !excluded.isEmpty()) {
      sql.append("AND i.id NOT IN (");
      for (int i = 0; i < excluded.size(); i++) {
        if (i > 0) sql.append(",");
        sql.append("?");
      }
      sql.append(") ");
      for (Integer id : excluded) {
        params.add(id);
      }
    }
    sql.append("ORDER BY i.endtime ASC LIMIT ?");
    params.add(need);
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      for (int i = 0; i < params.size(); i++) {
        Object p = params.get(i);
        bindParam(ps, i + 1, p);
      }
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          out.add(rs.getInt(1));
        }
      }
    } catch (SQLException e) {
      LOGGER.warn("fetchBackfillLiveItemIds failed", e);
    }
    return out;
  }

  private static void bindParam(PreparedStatement ps, int idx, Object p) throws SQLException {
    if (p instanceof String) {
      ps.setString(idx, (String) p);
    } else if (p instanceof Integer) {
      ps.setInt(idx, (Integer) p);
    } else if (p instanceof Double d) {
      ps.setDouble(idx, d);
    } else {
      ps.setObject(idx, p);
    }
  }

  private List<Item> loadLiveItemsPreservingOrder(List<Integer> ids) {
    List<Item> ans = new ArrayList<>();
    if (ids.isEmpty()) {
      return ans;
    }
    StringBuilder in = new StringBuilder();
    for (int i = 0; i < ids.size(); i++) {
      if (i > 0) in.append(",");
      in.append("?");
    }
    String sql =
        "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar FROM items i "
            + "LEFT JOIN users u ON i.sellerid = u.id WHERE i.id IN ("
            + in
            + ") AND i.status = 'OPEN' AND i.starttime <= NOW() AND i.endtime > NOW()";
    Object[] argv = ids.stream().map(i -> (Object) i).toArray();
    List<Item> rows = queryList(sql, argv);
    Map<Integer, Item> byId = new HashMap<>();
    for (Item row : rows) {
      byId.put(row.getId(), row);
    }
    for (int id : ids) {
      Item it = byId.get(id);
      if (it != null) ans.add(it);
    }
    return ans;
  }

  private List<Item> queryItems(String sql) {
    return queryList(sql);
  }

  private record TrendingAggRow(int itemId, double wLong, double wShort, int distinctBidders) {}
}
