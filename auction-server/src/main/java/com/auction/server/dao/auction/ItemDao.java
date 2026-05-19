package com.auction.server.dao.auction;

import com.auction.server.dao.platform.BaseDao;
import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import com.auction.shared.ItemFactory;
import com.auction.shared.ItemStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemDao extends BaseDao<Item> implements ItemRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(ItemDao.class);

  private static final String SELECT_ITEM_WITH_SELLER =
          "SELECT i.*, u.username AS seller_name, u.avatar_url AS seller_avatar "
                  + "FROM items i LEFT JOIN users u ON i.sellerid = u.id";

  private static final String SELECT_SELLER_ID_BY_USERNAME_SQL =
          "SELECT id FROM users WHERE username = ? LIMIT 1";

  private static final String INSERT_ITEM_SQL =
          "INSERT INTO items (category, auction_type, name, description, startingprice, "
                  + "currentprice, maxprice, dutch_reserve_price, dutch_tick_amount, "
                  + "dutch_tick_interval_mins, starttime, endtime, sellerid, status, version, image_url) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String UPDATE_PRICE_SQL =
          "UPDATE items SET currentprice = ?, version = version + 1 "
                  + "WHERE id = ? AND version = ?";

  private static final String UPDATE_END_TIME_SQL =
          "UPDATE items SET endtime = ? WHERE id = ?";

  private static final String CLOSE_AUCTION_SQL =
          "UPDATE items SET winnerid = ?, status = ? WHERE id = ?";

  private static final String ATOMIC_CLOSE_AUCTION_SQL =
          "UPDATE items SET winnerid = ?, status = ? WHERE id = ? AND status = 'OPEN'";

  private static final String APPROVE_ITEM_SQL =
          "UPDATE items SET status = 'OPEN' WHERE id = ? AND status = 'PENDING'";

  private static final String REJECT_ITEM_SQL =
          "UPDATE items SET status = 'CANCELED' WHERE id = ? AND status = 'PENDING'";

  private static final String STATUS_STATS_SQL =
          "SELECT status, COUNT(*) AS cnt FROM items GROUP BY status";

  private static final String CATEGORY_STATS_SQL =
          "SELECT category, SUM(currentprice) AS total FROM items "
                  + "WHERE status IN ('CLOSED', 'FINISHED') GROUP BY category";

  private static final String OPEN_AUCTION_IDS_BY_SELLER_SQL =
          "SELECT id FROM items WHERE sellerid = ? AND status = 'OPEN'";

  private static final String CANCEL_AUCTION_TX_SQL =
          "UPDATE items SET status = 'CANCELED' WHERE id = ? AND status = 'OPEN'";

  private static final String SELLER_CANCEL_PENDING_SQL =
          "UPDATE items SET status = 'CANCELED' "
                  + "WHERE id = ? AND sellerid = ? AND status = 'PENDING'";

  private static final String UPDATE_SELLER_LISTING_BEFORE_START_SQL =
          "UPDATE items SET category = ?, auction_type = ?, name = ?, description = ?, "
                  + "startingprice = ?, currentprice = ?, maxprice = ?, dutch_reserve_price = ?, "
                  + "dutch_tick_amount = ?, dutch_tick_interval_mins = ?, starttime = ?, endtime = ?, "
                  + "image_url = ?, version = version + 1 "
                  + "WHERE id = ? AND sellerid = ? "
                  + "AND (status = 'PENDING' OR (status = 'OPEN' AND starttime > NOW()))";

  @Override
  protected Item mapRow(ResultSet rs) throws SQLException {
    Item item = ItemFactory.createItem(rs.getString("category"));

    item.setId(rs.getInt("id"));
    item.setVersion(rs.getInt("version"));
    item.setName(rs.getString("name"));
    item.setDescription(rs.getString("description"));
    item.setStartingPrice(rs.getDouble("startingprice"));
    item.setCurrentPrice(rs.getDouble("currentprice"));
    item.setMaxPrice(rs.getDouble("maxprice"));

    Timestamp startTimestamp = rs.getTimestamp("starttime");
    if (startTimestamp != null) {
      item.setStartTime(startTimestamp.toLocalDateTime());
    }

    Timestamp endTimestamp = rs.getTimestamp("endtime");
    if (endTimestamp != null) {
      item.setEndTime(endTimestamp.toLocalDateTime());
    }

    item.setSellerId(rs.getInt("sellerid"));
    item.setWinnerId(rs.getInt("winnerid"));
    item.setStatus(ItemStatus.parse(rs.getString("status")));
    item.setImageUrl(rs.getString("image_url"));

    mapAuctionTypeFields(rs, item);
    mapSellerFields(rs, item);

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

  private void mapSellerFields(ResultSet rs, Item item) {
    try {
      item.setSellerUsername(rs.getString("seller_name"));
      item.setSellerAvatarUrl(rs.getString("seller_avatar"));
    } catch (SQLException e) {
      LOGGER.debug("Optional seller fields are not available in this query", e);
    }
  }

  @Override
  public List<Item> getAll() {
    return queryList(SELECT_ITEM_WITH_SELLER);
  }

  @Override
  public Item getById(int itemId) {
    return querySingle(SELECT_ITEM_WITH_SELLER + " WHERE i.id = ?", itemId);
  }

  @Override
  public List<Item> getBySellerId(int sellerId) {
    return queryList(
            SELECT_ITEM_WITH_SELLER + " WHERE i.sellerid = ? ORDER BY i.id DESC",
            sellerId);
  }

  @Override
  public List<Item> getPendingItems() {
    return queryList(
            SELECT_ITEM_WITH_SELLER + " WHERE i.status = 'PENDING' ORDER BY i.id DESC");
  }

  @Override
  public boolean updatePrice(int itemId, double price, int version) {
    return executeUpdate(UPDATE_PRICE_SQL, price, itemId, version);
  }

  @Override
  public boolean updateEndTime(int itemId, LocalDateTime endTime) {
    return executeUpdate(UPDATE_END_TIME_SQL, Timestamp.valueOf(endTime), itemId);
  }

  @Override
  public boolean insertLot(
          String name,
          String description,
          double startingPrice,
          double maxPrice,
          LocalDateTime startTime,
          LocalDateTime endTime,
          String sellerUsername,
          String imageUrl,
          String category,
          AuctionType auctionType,
          double dutchReserve,
          double dutchTick,
          int interval) {

    try (Connection conn = getConn()) {
      int sellerId = findSellerIdByUsername(conn, sellerUsername);
      if (sellerId <= 0) {
        return false;
      }

      AuctionType type = auctionType != null ? auctionType : AuctionType.ENGLISH;

      try (PreparedStatement ps = conn.prepareStatement(INSERT_ITEM_SQL)) {
        bindInsertLotParams(
                ps,
                name,
                description,
                startingPrice,
                maxPrice,
                startTime,
                endTime,
                sellerId,
                imageUrl,
                category,
                type,
                dutchReserve,
                dutchTick,
                interval);

        return ps.executeUpdate() > 0;
      }
    } catch (Exception e) {
      LOGGER.warn("insertLot failed for sellerUsername={}", sellerUsername, e);
      return false;
    }
  }

  private int findSellerIdByUsername(Connection conn, String sellerUsername) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement(SELECT_SELLER_ID_BY_USERNAME_SQL)) {
      ps.setString(1, sellerUsername);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1);
        }
      }
    }

    return -1;
  }

  private void bindInsertLotParams(
          PreparedStatement ps,
          String name,
          String description,
          double startingPrice,
          double maxPrice,
          LocalDateTime startTime,
          LocalDateTime endTime,
          int sellerId,
          String imageUrl,
          String category,
          AuctionType type,
          double dutchReserve,
          double dutchTick,
          int interval)
          throws SQLException {

    ps.setString(1, category == null ? "Vehicle" : category);
    ps.setString(2, type.dbName());
    ps.setString(3, name);
    ps.setString(4, description);
    ps.setDouble(5, startingPrice);
    ps.setDouble(6, startingPrice);
    ps.setDouble(7, maxPrice);

    bindDutchAuctionParams(ps, type, dutchReserve, dutchTick, interval, 8);

    ps.setTimestamp(11, Timestamp.valueOf(startTime));
    ps.setTimestamp(12, Timestamp.valueOf(endTime));
    ps.setInt(13, sellerId);
    ps.setString(14, ItemStatus.PENDING.name());
    ps.setInt(15, 0);
    ps.setString(16, imageUrl);
  }

  private void bindDutchAuctionParams(
          PreparedStatement ps,
          AuctionType type,
          double dutchReserve,
          double dutchTick,
          int interval,
          int startIndex)
          throws SQLException {

    if (type == AuctionType.DUTCH) {
      ps.setDouble(startIndex, dutchReserve);
      ps.setDouble(startIndex + 1, dutchTick);
      ps.setInt(startIndex + 2, interval);
      return;
    }

    ps.setNull(startIndex, Types.DOUBLE);
    ps.setNull(startIndex + 1, Types.DOUBLE);
    ps.setNull(startIndex + 2, Types.INTEGER);
  }

  @Override
  public void closeAuction(int itemId, int winnerId, String status) {
    executeUpdate(CLOSE_AUCTION_SQL, winnerId, status, itemId);
  }

  public boolean atomicCloseAuction(int itemId, int winnerId, String newStatus) {
    return executeUpdate(ATOMIC_CLOSE_AUCTION_SQL, winnerId, newStatus, itemId);
  }

  @Override
  public boolean approveItem(int itemId) {
    return executeUpdate(APPROVE_ITEM_SQL, itemId);
  }

  @Override
  public boolean rejectItem(int itemId) {
    return executeUpdate(REJECT_ITEM_SQL, itemId);
  }

  @Override
  public HashMap<String, Integer> getStatusStats() {
    HashMap<String, Integer> stats = new HashMap<>();

    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(STATUS_STATS_SQL);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        stats.put(rs.getString("status"), rs.getInt("cnt"));
      }
    } catch (Exception e) {
      LOGGER.warn("getStatusStats failed", e);
    }

    return stats;
  }

  @Override
  public HashMap<String, Double> getCategoryStats() {
    HashMap<String, Double> stats = new HashMap<>();

    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(CATEGORY_STATS_SQL);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        stats.put(rs.getString("category"), rs.getDouble("total"));
      }
    } catch (Exception e) {
      LOGGER.warn("getCategoryStats failed", e);
    }

    return stats;
  }

  public List<Integer> getOpenAuctionIdsBySeller(int sellerId) {
    List<Integer> ids = new ArrayList<>();

    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(OPEN_AUCTION_IDS_BY_SELLER_SQL)) {

      ps.setInt(1, sellerId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          ids.add(rs.getInt(1));
        }
      }
    } catch (Exception e) {
      LOGGER.warn("getOpenAuctionIdsBySeller failed for sellerId={}", sellerId, e);
    }

    return ids;
  }

  public Item getByIdTx(int itemId, Connection conn) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement(SELECT_ITEM_WITH_SELLER + " WHERE i.id = ?")) {
      ps.setInt(1, itemId);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapRow(rs);
        }
      }
    }

    return null;
  }

  public boolean cancelAuctionTx(int itemId, Connection conn) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement(CANCEL_AUCTION_TX_SQL)) {
      ps.setInt(1, itemId);
      return ps.executeUpdate() > 0;
    }
  }

  public boolean sellerCancelPending(int itemId, int sellerId) {
    return executeUpdate(SELLER_CANCEL_PENDING_SQL, itemId, sellerId);
  }

  public boolean updateSellerListingBeforeStartBySeller(
          int itemId,
          int sellerId,
          String name,
          String description,
          double startingPrice,
          double maxPrice,
          LocalDateTime startTime,
          LocalDateTime endTime,
          String imageUrl,
          String category,
          AuctionType auctionType,
          double dutchReserve,
          double dutchTick,
          int dutchIntervalMin) {

    AuctionType type = auctionType != null ? auctionType : AuctionType.ENGLISH;

    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(UPDATE_SELLER_LISTING_BEFORE_START_SQL)) {

      ps.setString(1, category == null ? "Vehicle" : category);
      ps.setString(2, type.dbName());
      ps.setString(3, name);
      ps.setString(4, description);
      ps.setDouble(5, startingPrice);
      ps.setDouble(6, startingPrice);
      ps.setDouble(7, type == AuctionType.DUTCH ? 0 : maxPrice);

      bindDutchAuctionParams(ps, type, dutchReserve, dutchTick, dutchIntervalMin, 8);

      ps.setTimestamp(11, Timestamp.valueOf(startTime));
      ps.setTimestamp(12, Timestamp.valueOf(endTime));
      ps.setString(13, imageUrl != null ? imageUrl : "");
      ps.setInt(14, itemId);
      ps.setInt(15, sellerId);

      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      LOGGER.warn(
              "updateSellerListingBeforeStartBySeller failed for itemId={}, sellerId={}",
              itemId,
              sellerId,
              e);
      return false;
    }
  }

  public boolean updatePriceTx(int itemId, double price, int version, Connection conn)
          throws SQLException {

    try (PreparedStatement ps = conn.prepareStatement(UPDATE_PRICE_SQL)) {
      ps.setDouble(1, price);
      ps.setInt(2, itemId);
      ps.setInt(3, version);

      return ps.executeUpdate() > 0;
    }
  }
}