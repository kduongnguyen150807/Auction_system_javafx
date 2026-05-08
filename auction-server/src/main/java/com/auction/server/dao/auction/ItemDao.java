package com.auction.server.dao.auction;

import com.auction.server.dao.platform.BaseDao;

import com.auction.shared.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemDao extends BaseDao<Item> implements ItemRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(ItemDao.class);
  private static final String JOIN_SELLER =
      "select i.*, u.username as seller_name, u.avatar_url as seller_avatar from items i left join users u on i.sellerid = u.id";

  @Override
  protected Item mapRow(ResultSet rs) throws SQLException {
    Item item = ItemFactory.createItem(rs.getString("category"));
    item.setId(rs.getInt("id")); item.setVersion(rs.getInt("version"));
    item.setName(rs.getString("name")); item.setDescription(rs.getString("description"));
    item.setStartingPrice(rs.getDouble("startingprice")); item.setCurrentPrice(rs.getDouble("currentprice"));
    item.setMaxPrice(rs.getDouble("maxprice"));
    Timestamp startTs = rs.getTimestamp("starttime"); if (startTs != null) item.setStartTime(startTs.toLocalDateTime());
    Timestamp endTs = rs.getTimestamp("endtime"); if (endTs != null) item.setEndTime(endTs.toLocalDateTime());
    item.setSellerId(rs.getInt("sellerid")); item.setWinnerId(rs.getInt("winnerid"));
    item.setStatus(ItemStatus.parse(rs.getString("status")));
    item.setImageUrl(rs.getString("image_url"));
    try {
      item.setAuctionType(AuctionType.parse(rs.getString("auction_type")));
      double dr = rs.getDouble("dutch_reserve_price");
      if (!rs.wasNull()) item.setDutchReservePrice(dr);
      double da = rs.getDouble("dutch_tick_amount");
      if (!rs.wasNull()) item.setDutchTickAmount(da);
      int im = rs.getInt("dutch_tick_interval_mins");
      if (!rs.wasNull()) item.setDutchTickIntervalMinutes(im);
    } catch (SQLException ignored) {
      /* Pre-migration schemas: keep ENGLISH defaults */
    }
    try {
      String sn = rs.getString("seller_name"); if (sn != null) item.setSellerUsername(sn);
      String sa = rs.getString("seller_avatar"); if (sa != null) item.setSellerAvatarUrl(sa);
    } catch (SQLException e) {}
    return item;
  }

  @Override
  public List<Item> getAll() {
    return queryList(JOIN_SELLER);
  }

  @Override
  public Item getById(int itemId) {
    return querySingle(JOIN_SELLER + " where i.id = ?", itemId);
  }

  @Override
  public List<Item> getBySellerId(int sellerId) {
    return queryList("SELECT * FROM items WHERE sellerid = ? ORDER BY id DESC", sellerId);
  }

  @Override
  public List<Item> getExpiredItems() {
    return queryList("SELECT * FROM items WHERE endtime <= NOW() AND status = 'OPEN'");
  }

  @Override
  public List<Item> getPendingItems() {
    return queryList(JOIN_SELLER + " where i.status = 'PENDING' order by i.id desc");
  }

  @Override
  public boolean updatePrice(int itemId, double price, int version) {
    return executeUpdate("update items set currentprice = ?, version = version + 1 where id = ? and version = ?", price,
        itemId, version);
  }

  @Override
  public boolean updateEndTime(int itemId, LocalDateTime endTime) {
    return executeUpdate("update items set endtime = ? where id = ?", Timestamp.valueOf(endTime), itemId);
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
      double dutchReservePrice,
      double dutchTickAmount,
      int dutchTickIntervalMinutes) {
    try (Connection conn = getConn()) {
      int sellerId = -1;
      try (PreparedStatement lookup = conn.prepareStatement("select id from users where username = ? limit 1")) {
        lookup.setString(1, sellerUsername);
        ResultSet rs = lookup.executeQuery();
        if (rs.next()) sellerId = rs.getInt(1);
      }
      if (sellerId <= 0) return false;
      AuctionType kind = auctionType != null ? auctionType : AuctionType.ENGLISH;
      String sql =
          "INSERT INTO items (category, auction_type, name, description, startingprice, currentprice,"
              + " maxprice, dutch_reserve_price, dutch_tick_amount, dutch_tick_interval_mins,"
              + " starttime, endtime, sellerid, winnerid, status, version, image_url)"
              + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, category == null ? "Vehicle" : category);
        ps.setString(2, kind.dbName());
        ps.setString(3, name);
        ps.setString(4, description);
        ps.setDouble(5, startingPrice);
        ps.setDouble(6, startingPrice);
        ps.setDouble(7, maxPrice);
        if (kind == AuctionType.DUTCH) {
          ps.setDouble(8, dutchReservePrice);
          ps.setDouble(9, dutchTickAmount);
          ps.setInt(10, dutchTickIntervalMinutes);
        } else {
          ps.setNull(8, Types.DOUBLE);
          ps.setNull(9, Types.DOUBLE);
          ps.setNull(10, Types.INTEGER);
        }
        ps.setTimestamp(11, Timestamp.valueOf(startTime));
        ps.setTimestamp(12, Timestamp.valueOf(endTime));
        ps.setInt(13, sellerId);
        ps.setNull(14, Types.INTEGER);
        ps.setString(15, ItemStatus.PENDING.name());
        ps.setInt(16, 0);
        ps.setString(17, imageUrl);
        return ps.executeUpdate() > 0;
      }
    } catch (Exception e) {
      LOGGER.warn("insertLot failed", e);
      return false;
    }
  }

  @Override
  public void closeAuction(int itemId, int winnerId, String status) {
    executeUpdate("update items set winnerid = ?, status = ? where id = ?", winnerId, status, itemId);
  }

  public boolean atomicCloseAuction(int itemId, int winnerId, String newStatus) {
    return executeUpdate(
        "UPDATE items SET winnerid = ?, status = ? WHERE id = ? AND status = 'OPEN'", winnerId, newStatus, itemId);
  }

  @Override
  public boolean approveItem(int itemId) {
    return executeUpdate("update items set status = 'OPEN' where id = ? and status = 'PENDING'", itemId);
  }

  @Override
  public boolean rejectItem(int itemId) {
    return executeUpdate("update items set status = 'CANCELED' where id = ? and status = 'PENDING'", itemId);
  }

  @Override
  public HashMap<String, Integer> getStatusStats() {
    HashMap<String, Integer> stats = new HashMap<>();
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(
        "select status, count(*) as cnt from items group by status")) {
      ResultSet rs = ps.executeQuery();
      while (rs.next()) stats.put(rs.getString("status"), rs.getInt("cnt"));
    } catch (Exception e) { LOGGER.warn("getStatusStats failed", e); }
    return stats;
  }

  @Override
  public HashMap<String, Double> getCategoryStats() {
    HashMap<String, Double> stats = new HashMap<>();
    String sql = "select category, sum(currentprice) as total from items where status in ('CLOSED', 'FINISHED') group by category";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ResultSet rs = ps.executeQuery();
      while (rs.next()) stats.put(rs.getString("category"), rs.getDouble("total"));
    } catch (Exception e) { LOGGER.warn("getCategoryStats failed", e); }
    return stats;
  }

  public List<Integer> getOpenAuctionIdsBySeller(int sellerId) {
    List<Integer> ids = new ArrayList<>();
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(
        "SELECT id FROM items WHERE sellerid = ? AND status = 'OPEN'")) {
      ps.setInt(1, sellerId); ResultSet rs = ps.executeQuery();
      while (rs.next()) ids.add(rs.getInt(1));
    } catch (Exception e) { LOGGER.warn("getOpenAuctionIdsBySeller failed", e); }
    return ids;
  }

  public Item getByIdTx(int itemId, Connection conn) throws SQLException {
    String sql =
        "SELECT i.*, u.username AS seller_name, u.avatar_url AS seller_avatar FROM items i LEFT JOIN users u ON i.sellerid = u.id WHERE i.id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return mapRow(rs);
      }
    }
    return null;
  }

  public boolean cancelAuctionTx(int itemId, Connection conn) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("UPDATE items SET status = 'CANCELED' WHERE id = ? AND status = 'OPEN'")) {
      ps.setInt(1, itemId); return ps.executeUpdate() > 0;
    }
  }

  public boolean updatePriceTx(int itemId, double price, Connection conn) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("UPDATE items SET currentprice = ?, version = version + 1 WHERE id = ?")) {
      ps.setDouble(1, price); ps.setInt(2, itemId); return ps.executeUpdate() > 0;
    }
  }

}
