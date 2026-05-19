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

public class LotDao extends BaseDao<Item> implements LotRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(LotDao.class);

  private static final String SELECT_LOT_WITH_SELLER =
          "SELECT i.*, u.username AS s_name, u.avatar_url AS s_avatar "
                  + "FROM items i LEFT JOIN users u ON i.sellerid = u.id ";

  private static final String SELECT_LOT_WITH_SELLER_AND_WINNER =
          "SELECT i.*, u.username AS s_name, u.avatar_url AS s_avatar, w.username AS w_name "
                  + "FROM items i "
                  + "LEFT JOIN users u ON i.sellerid = u.id "
                  + "LEFT JOIN users w ON i.winnerid = w.id ";

  private static final String ONGOING_BIDS_CONDITION =
          "WHERE i.status = 'OPEN' AND i.starttime <= NOW() AND i.endtime > NOW()";

  private static final String UPCOMING_BIDS_CONDITION =
          "WHERE i.status = 'OPEN' AND i.starttime > NOW()";

  private static final String CLOSED_BIDS_CONDITION =
          "WHERE i.status = 'CLOSED'";

  private static final String PAST_BIDS_CONDITION =
          "WHERE i.status IN ('FINISHED', 'CANCELED') "
                  + "OR (i.status = 'OPEN' AND i.endtime <= NOW())";

  private static final String TRENDING_AGGREGATES_SQL =
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

  @Override
  protected Item mapRow(ResultSet rs) throws SQLException {
    Item item = ItemFactory.createItem(rs.getString("category"));

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

    Timestamp startTimestamp = rs.getTimestamp("starttime");
    if (startTimestamp != null) {
      item.setStartTime(startTimestamp.toLocalDateTime());
    }

    Timestamp endTimestamp = rs.getTimestamp("endtime");
    if (endTimestamp != null) {
      item.setEndTime(endTimestamp.toLocalDateTime());
    }

    item.setStatus(ItemStatus.parse(rs.getString("status")));

    mapAuctionTypeFields(rs, item);
    mapWinnerFields(rs, item);

    return item;
  }

  private void mapAuctionTypeFields(ResultSet rs, Item item) {
    try {
      item.setAuctionType(AuctionType.parse(rs.getString("auction_type")));

      double dutchReservePrice = rs.getDouble("dutch_reserve_price");
      if (!rs.wasNull()) {
        item.setDutchReservePrice(dutchReservePrice);
      }

      double dutchTickAmount = rs.getDouble("dutch_tick_amount");
      if (!rs.wasNull()) {
        item.setDutchTickAmount(dutchTickAmount);
      }

      int dutchTickIntervalMinutes = rs.getInt("dutch_tick_interval_mins");
      if (!rs.wasNull()) {
        item.setDutchTickIntervalMinutes(dutchTickIntervalMinutes);
      }
    } catch (SQLException e) {
      LOGGER.debug("Optional auction type fields are not available in this query", e);
    }
  }

  private void mapWinnerFields(ResultSet rs, Item item) {
    try {
      String winnerName = rs.getString("w_name");
      if (winnerName != null) {
        item.setWinnerUsername(winnerName);
      }
    } catch (SQLException e) {
      LOGGER.debug("Optional winner field is not available in this query", e);
    }
  }

  @Override
  public List<Item> getOngoingBids(int userId) {
    return queryItems(SELECT_LOT_WITH_SELLER + ONGOING_BIDS_CONDITION);
  }

  @Override
  public List<Item> getUpcomingBids(int userId) {
    return queryItems(SELECT_LOT_WITH_SELLER + UPCOMING_BIDS_CONDITION);
  }

  @Override
  public List<Item> getClosedBids(int userId) {
    return queryItems(SELECT_LOT_WITH_SELLER_AND_WINNER + CLOSED_BIDS_CONDITION);
  }

  @Override
  public List<Item> getPastBids(int userId) {
    return queryItems(SELECT_LOT_WITH_SELLER_AND_WINNER + PAST_BIDS_CONDITION);
  }

  @Override
  public List<Item> getTrendingLiveItems(AuctionType auctionType, int limit) {
    AuctionType type = auctionType != null ? auctionType : AuctionType.ENGLISH;
    int safeLimit = Math.max(1, Math.min(limit, 20));
    String auctionTypeName = type.dbName();

    List<TrendingAggRow> aggregateRows = fetchTrendingAggregates(auctionTypeName);

    aggregateRows.sort(
            (first, second) ->
                    Double.compare(
                            calculateTrendScore(second),
                            calculateTrendScore(first)));

    List<Integer> orderedIds = collectTrendingItemIds(aggregateRows, safeLimit);

    if (orderedIds.size() < safeLimit) {
      int missingCount = safeLimit - orderedIds.size();
      orderedIds.addAll(fetchBackfillLiveItemIds(auctionTypeName, orderedIds, missingCount));
    }

    return loadLiveItemsPreservingOrder(orderedIds);
  }

  private double calculateTrendScore(TrendingAggRow row) {
    return TrendingLotsFormula.computeTrendScore(
            row.wLong(),
            row.wShort(),
            row.distinctBidders());
  }

  private List<Integer> collectTrendingItemIds(
          List<TrendingAggRow> aggregateRows,
          int limit) {

    List<Integer> orderedIds = new ArrayList<>();

    for (TrendingAggRow row : aggregateRows) {
      if (orderedIds.size() >= limit) {
        break;
      }

      double score = calculateTrendScore(row);
      if (score > TrendingLotsFormula.EPS) {
        orderedIds.add(row.itemId());
      }
    }

    return orderedIds;
  }

  private List<TrendingAggRow> fetchTrendingAggregates(String auctionTypeName) {
    List<TrendingAggRow> rows = new ArrayList<>();

    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(TRENDING_AGGREGATES_SQL)) {

      ps.setDouble(1, TrendingLotsFormula.TAU_DECAY_HOURS);
      ps.setInt(2, TrendingLotsFormula.SHORT_WINDOW_MINUTES);
      ps.setDouble(3, TrendingLotsFormula.TAU_DECAY_HOURS);
      ps.setString(4, auctionTypeName);
      ps.setInt(5, TrendingLotsFormula.LONG_WINDOW_HOURS);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          rows.add(
                  new TrendingAggRow(
                          rs.getInt("item_id"),
                          rs.getDouble("w_long"),
                          rs.getDouble("w_short"),
                          rs.getInt("u_cnt")));
        }
      }
    } catch (SQLException e) {
      LOGGER.warn("fetchTrendingAggregates failed for auctionType={}", auctionTypeName, e);
    }

    return rows;
  }

  private List<Integer> fetchBackfillLiveItemIds(
          String auctionTypeName,
          List<Integer> excludedIds,
          int neededCount) {

    List<Integer> ids = new ArrayList<>();

    if (neededCount <= 0) {
      return ids;
    }

    StringBuilder sql =
            new StringBuilder(
                    "SELECT i.id FROM items i "
                            + "WHERE i.status = 'OPEN' "
                            + "AND i.starttime <= NOW() "
                            + "AND i.endtime > NOW() "
                            + "AND i.auction_type = ? ");

    List<Object> params = new ArrayList<>();
    params.add(auctionTypeName);

    appendExcludedIdsCondition(sql, params, excludedIds);

    sql.append("ORDER BY i.endtime ASC LIMIT ?");
    params.add(neededCount);

    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(sql.toString())) {

      bindParams(ps, params);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          ids.add(rs.getInt(1));
        }
      }
    } catch (SQLException e) {
      LOGGER.warn("fetchBackfillLiveItemIds failed for auctionType={}", auctionTypeName, e);
    }

    return ids;
  }

  private void appendExcludedIdsCondition(
          StringBuilder sql,
          List<Object> params,
          List<Integer> excludedIds) {

    if (excludedIds == null || excludedIds.isEmpty()) {
      return;
    }

    sql.append("AND i.id NOT IN (");

    for (int i = 0; i < excludedIds.size(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      sql.append("?");
      params.add(excludedIds.get(i));
    }

    sql.append(") ");
  }

  private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
    for (int i = 0; i < params.size(); i++) {
      bindParam(ps, i + 1, params.get(i));
    }
  }

  private static void bindParam(PreparedStatement ps, int index, Object value)
          throws SQLException {

    if (value instanceof String stringValue) {
      ps.setString(index, stringValue);
    } else if (value instanceof Integer integerValue) {
      ps.setInt(index, integerValue);
    } else if (value instanceof Double doubleValue) {
      ps.setDouble(index, doubleValue);
    } else {
      ps.setObject(index, value);
    }
  }

  private List<Item> loadLiveItemsPreservingOrder(List<Integer> ids) {
    List<Item> orderedItems = new ArrayList<>();

    if (ids == null || ids.isEmpty()) {
      return orderedItems;
    }

    String placeholders = buildPlaceholders(ids.size());

    String sql =
            SELECT_LOT_WITH_SELLER
                    + "WHERE i.id IN ("
                    + placeholders
                    + ") AND i.status = 'OPEN' "
                    + "AND i.starttime <= NOW() "
                    + "AND i.endtime > NOW()";

    Object[] params = ids.stream().map(id -> (Object) id).toArray();
    List<Item> rows = queryList(sql, params);

    Map<Integer, Item> itemById = new HashMap<>();
    for (Item item : rows) {
      itemById.put(item.getId(), item);
    }

    for (Integer id : ids) {
      Item item = itemById.get(id);
      if (item != null) {
        orderedItems.add(item);
      }
    }

    return orderedItems;
  }

  private String buildPlaceholders(int count) {
    StringBuilder placeholders = new StringBuilder();

    for (int i = 0; i < count; i++) {
      if (i > 0) {
        placeholders.append(", ");
      }
      placeholders.append("?");
    }

    return placeholders.toString();
  }

  private List<Item> queryItems(String sql) {
    return queryList(sql);
  }

  public List<Item> getWatchlistItems(int userId) {
    String sql =
            SELECT_LOT_WITH_SELLER
                    + "JOIN watchlists w ON i.id = w.item_id "
                    + "WHERE w.user_id = ? "
                    + "ORDER BY w.created_at DESC";

    return queryList(sql, userId);
  }

  private record TrendingAggRow(
          int itemId,
          double wLong,
          double wShort,
          int distinctBidders) {}
}