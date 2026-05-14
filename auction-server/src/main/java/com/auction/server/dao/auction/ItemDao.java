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

/**
 * Data Access Object for Items.
 * Implements Optimistic Locking using the 'version' column.
 */
public class ItemDao extends BaseDao<Item> implements ItemRepository {

  private static final String SELECT_ITEM_WITH_SELLER =
          "SELECT i.*, u.username AS seller_name, u.avatar_url AS seller_avatar "
                  + "FROM items i LEFT JOIN users u ON i.sellerid = u.id";

  @Override
  protected Item mapRow(ResultSet rs) throws SQLException {
    Item item = ItemFactory.createItem(rs.getString("category"));
    item.setId(rs.getInt("id"));
    item.setVersion(rs.getInt("version")); // Quan trọng cho Optimistic Locking
    item.setName(rs.getString("name"));
    item.setDescription(rs.getString("description"));
    item.setStartingPrice(rs.getDouble("startingprice"));
    item.setCurrentPrice(rs.getDouble("currentprice"));
    item.setMaxPrice(rs.getDouble("maxprice"));

    Timestamp startTs = rs.getTimestamp("starttime");
    if (startTs != null) {
      item.setStartTime(startTs.toLocalDateTime());
    }
    Timestamp endTs = rs.getTimestamp("endtime");
    if (endTs != null) {
      item.setEndTime(endTs.toLocalDateTime());
    }

    item.setSellerId(rs.getInt("sellerid"));
    item.setWinnerId(rs.getInt("winnerid"));
    item.setStatus(ItemStatus.parse(rs.getString("status")));
    item.setImageUrl(rs.getString("image_url"));

    try {
      item.setAuctionType(AuctionType.parse(rs.getString("auction_type")));
      double dr = rs.getDouble("dutch_reserve_price");
      if (!rs.wasNull()) {
        item.setDutchReservePrice(dr);
      }
      double da = rs.getDouble("dutch_tick_amount");
      if (!rs.wasNull()) {
        item.setDutchTickAmount(da);
      }
      int im = rs.getInt("dutch_tick_interval_mins");
      if (!rs.wasNull()) {
        item.setDutchTickIntervalMinutes(im);
      }
    } catch (SQLException ignored) {
      // Optional columns
    }

    try {
      item.setSellerUsername(rs.getString("seller_name"));
      item.setSellerAvatarUrl(rs.getString("seller_avatar"));
    } catch (SQLException ignored) {
      // Optional join columns
    }
    return item;
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
    return queryList(SELECT_ITEM_WITH_SELLER + " WHERE i.sellerid = ? ORDER BY i.id DESC", sellerId);
  }

  @Override
  public List<Item> getPendingItems() {
    return queryList(SELECT_ITEM_WITH_SELLER + " WHERE i.status = 'PENDING' ORDER BY i.id DESC");
  }

  /**
   * Updates price using Optimistic Locking.
   */
  @Override
  public boolean updatePrice(int itemId, double price, int version) {
    String sql = "UPDATE items SET currentprice = ?, version = version + 1 "
            + "WHERE id = ? AND version = ?";
    return executeUpdate(sql, price, itemId, version);
  }

  @Override
  public boolean updateEndTime(int itemId, LocalDateTime endTime) {
    return executeUpdate("UPDATE items SET endtime = ? WHERE id = ?",
            Timestamp.valueOf(endTime), itemId);
  }

  @Override
  public boolean insertLot(String name, String description, double startingPrice, double maxPrice,
                           LocalDateTime startTime, LocalDateTime endTime, String sellerUsername, String imageUrl,
                           String category, AuctionType auctionType, double dutchReserve, double dutchTick, int interval) {

    try (Connection conn = getConn()) {
      int sellerId = -1;
      try (PreparedStatement lookup = conn.prepareStatement(
              "SELECT id FROM users WHERE username = ? LIMIT 1")) {
        lookup.setString(1, sellerUsername);
        ResultSet rs = lookup.executeQuery();
        if (rs.next()) {
          sellerId = rs.getInt(1);
        }
      }

      if (sellerId <= 0) {
        return false;
      }

      AuctionType type = (auctionType != null) ? auctionType : AuctionType.ENGLISH;
      String sql = "INSERT INTO items (category, auction_type, name, description, startingprice, "
              + "currentprice, maxprice, dutch_reserve_price, dutch_tick_amount, "
              + "dutch_tick_interval_mins, starttime, endtime, sellerid, status, version, image_url) "
              + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, (category == null) ? "Vehicle" : category);
        ps.setString(2, type.dbName());
        ps.setString(3, name);
        ps.setString(4, description);
        ps.setDouble(5, startingPrice);
        ps.setDouble(6, startingPrice);
        ps.setDouble(7, maxPrice);

        if (type == AuctionType.DUTCH) {
          ps.setDouble(8, dutchReserve);
          ps.setDouble(9, dutchTick);
          ps.setInt(10, interval);
        } else {
          ps.setNull(8, Types.DOUBLE);
          ps.setNull(9, Types.DOUBLE);
          ps.setNull(10, Types.INTEGER);
        }

        ps.setTimestamp(11, Timestamp.valueOf(startTime));
        ps.setTimestamp(12, Timestamp.valueOf(endTime));
        ps.setInt(13, sellerId);
        ps.setString(14, ItemStatus.PENDING.name());
        ps.setInt(15, 0); // Initial version
        ps.setString(16, imageUrl);

        return ps.executeUpdate() > 0;
      }
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void closeAuction(int itemId, int winnerId, String status) {
    executeUpdate("UPDATE items SET winnerid = ?, status = ? WHERE id = ?",
            winnerId, status, itemId);
  }

  public boolean atomicCloseAuction(int itemId, int winnerId, String newStatus) {
    return executeUpdate("UPDATE items SET winnerid = ?, status = ? WHERE id = ? AND status = 'OPEN'",
            winnerId, newStatus, itemId);
  }

  @Override
  public boolean approveItem(int itemId) {
    return executeUpdate("UPDATE items SET status = 'OPEN' WHERE id = ? AND status = 'PENDING'",
            itemId);
  }

  @Override
  public boolean rejectItem(int itemId) {
    return executeUpdate("UPDATE items SET status = 'CANCELED' WHERE id = ? AND status = 'PENDING'",
            itemId);
  }

  @Override
  public HashMap<String, Integer> getStatusStats() {
    HashMap<String, Integer> stats = new HashMap<>();
    String sql = "SELECT status, COUNT(*) AS cnt FROM items GROUP BY status";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        stats.put(rs.getString("status"), rs.getInt("cnt"));
      }
    } catch (Exception ignored) {}
    return stats;
  }

  @Override
  public HashMap<String, Double> getCategoryStats() {
    HashMap<String, Double> stats = new HashMap<>();
    String sql = "SELECT category, SUM(currentprice) AS total FROM items "
            + "WHERE status IN ('CLOSED', 'FINISHED') GROUP BY category";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        stats.put(rs.getString("category"), rs.getDouble("total"));
      }
    } catch (Exception ignored) {}
    return stats;
  }

  public List<Integer> getOpenAuctionIdsBySeller(int sellerId) {
    List<Integer> ids = new ArrayList<>();
    String sql = "SELECT id FROM items WHERE sellerid = ? AND status = 'OPEN'";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, sellerId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        ids.add(rs.getInt(1));
      }
    } catch (Exception ignored) {}
    return ids;
  }

  public Item getByIdTx(int itemId, Connection conn) throws SQLException {
    String sql = SELECT_ITEM_WITH_SELLER + " WHERE i.id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
    String sql = "UPDATE items SET status = 'CANCELED' WHERE id = ? AND status = 'OPEN'";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemId);
      return ps.executeUpdate() > 0;
    }
  }

  /** PENDING listing withdrawn by seller. */
  public boolean sellerCancelPending(int itemId, int sellerId) {
    String sql =
        "UPDATE items SET status = 'CANCELED' WHERE id = ? AND sellerid = ? AND status = 'PENDING'";
    return executeUpdate(sql, itemId, sellerId);
  }

  /**
   * Seller updates listing while it is not live yet: {@code PENDING}, or {@code OPEN} with {@code
   * starttime} still in the future (before the auction has started).
   */
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

    AuctionType type = (auctionType != null) ? auctionType : AuctionType.ENGLISH;
    String sql =
        "UPDATE items SET category=?, auction_type=?, name=?, description=?, "
            + "startingprice=?, currentprice=?, maxprice=?, dutch_reserve_price=?, dutch_tick_amount=?, "
            + "dutch_tick_interval_mins=?, starttime=?, endtime=?, image_url=?, version=version+1 "
            + "WHERE id=? AND sellerid=? AND (status='PENDING' OR (status='OPEN' AND starttime > NOW()))";

    try (Connection conn = getConn();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, (category == null) ? "Vehicle" : category);
      ps.setString(2, type.dbName());
      ps.setString(3, name);
      ps.setString(4, description);
      ps.setDouble(5, startingPrice);
      ps.setDouble(6, startingPrice);
      ps.setDouble(7, type == AuctionType.DUTCH ? 0 : maxPrice);

      if (type == AuctionType.DUTCH) {
        ps.setDouble(8, dutchReserve);
        ps.setDouble(9, dutchTick);
        ps.setInt(10, dutchIntervalMin);
      } else {
        ps.setNull(8, Types.DOUBLE);
        ps.setNull(9, Types.DOUBLE);
        ps.setNull(10, Types.INTEGER);
      }

      ps.setTimestamp(11, Timestamp.valueOf(startTime));
      ps.setTimestamp(12, Timestamp.valueOf(endTime));
      ps.setString(13, imageUrl != null ? imageUrl : "");
      ps.setInt(14, itemId);
      ps.setInt(15, sellerId);

      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Transactional update with Optimistic Locking.
   */
  public boolean updatePriceTx(int itemId, double price, int version, Connection conn)
          throws SQLException {
    String sql = "UPDATE items SET currentprice = ?, version = version + 1 "
            + "WHERE id = ? AND version = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setDouble(1, price);
      ps.setInt(2, itemId);
      ps.setInt(3, version);
      return ps.executeUpdate() > 0;
    }
  }
}