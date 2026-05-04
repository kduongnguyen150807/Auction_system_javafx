package com.auction.server.dao;

import com.auction.shared.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemDao implements ItemRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(ItemDao.class);
  private static final String JOIN_SELLER = "select i.*, u.username as seller_name, u.avatar_url as seller_avatar from items i left join users u on i.sellerid = u.id";

  private Connection getConn() { return DatabaseConnection.getInstance().getConnection(); }

  @Override
  public List<Item> getAll() {
    List<Item> items = new ArrayList<>();
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(JOIN_SELLER)) {
      ResultSet rs = ps.executeQuery();
      while (rs.next()) items.add(mapResultSetToItem(rs));
    } catch (Exception e) { LOGGER.warn("getAll items failed", e); }
    return items;
  }

  @Override
  public Item getById(int itemId) {
    String sql = JOIN_SELLER + " where i.id = ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemId); ResultSet rs = ps.executeQuery();
      if (rs.next()) return mapResultSetToItem(rs);
    } catch (Exception e) { LOGGER.warn("getById failed for id={}", itemId, e); }
    return null;
  }

  @Override
  public List<Item> getBySellerId(int sellerId) {
    List<Item> items = new ArrayList<>();
    String sql = "SELECT * FROM items WHERE sellerid = ? ORDER BY id DESC";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, sellerId); ResultSet rs = ps.executeQuery();
      while (rs.next()) items.add(mapResultSetToItem(rs));
    } catch (Exception e) { LOGGER.warn("getBySellerId failed", e); }
    return items;
  }

  @Override
  public List<Item> getExpiredItems() {
    List<Item> items = new ArrayList<>();
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(
        "SELECT * FROM items WHERE endtime <= NOW() AND status = 'OPEN'")) {
      ResultSet rs = ps.executeQuery();
      while (rs.next()) items.add(mapResultSetToItem(rs));
    } catch (Exception e) { LOGGER.warn("getExpiredItems failed", e); }
    return items;
  }

  @Override
  public List<Item> getPendingItems() {
    List<Item> items = new ArrayList<>();
    String sql = JOIN_SELLER + " where i.status = 'PENDING' order by i.id desc";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ResultSet rs = ps.executeQuery();
      while (rs.next()) items.add(mapResultSetToItem(rs));
    } catch (Exception e) { LOGGER.warn("getPendingItems failed", e); }
    return items;
  }

  @Override
  public boolean updatePrice(int itemId, double price, int version) {
    String sql = "update items set currentprice = ?, version = version + 1 where id = ? and version = ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setDouble(1, price); ps.setInt(2, itemId); ps.setInt(3, version); return ps.executeUpdate() > 0;
    } catch (Exception e) { LOGGER.warn("updatePrice failed", e); return false; }
  }

  @Override
  public boolean updateEndTime(int itemId, LocalDateTime endTime) {
    String sql = "update items set endtime = ? where id = ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setTimestamp(1, Timestamp.valueOf(endTime)); ps.setInt(2, itemId); return ps.executeUpdate() > 0;
    } catch (Exception e) { LOGGER.warn("updateEndTime failed", e); return false; }
  }

  @Override
  public boolean insertLot(String name, String description, double startingPrice, double maxPrice,
      LocalDateTime startTime, LocalDateTime endTime, String sellerUsername, String imageUrl, String category) {
    try (Connection conn = getConn()) {
      int sellerId = -1;
      try (PreparedStatement lookup = conn.prepareStatement("select id from users where username = ? limit 1")) {
        lookup.setString(1, sellerUsername);
        ResultSet rs = lookup.executeQuery();
        if (rs.next()) sellerId = rs.getInt(1);
      }
      if (sellerId <= 0) return false;
      String sql = "INSERT INTO items (category, name, description, startingprice, currentprice, maxprice, starttime, endtime, sellerid, winnerid, status, version, image_url) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, category == null ? "Vehicle" : category); ps.setString(2, name);
        ps.setString(3, description); ps.setDouble(4, startingPrice); ps.setDouble(5, startingPrice);
        ps.setDouble(6, maxPrice); ps.setTimestamp(7, Timestamp.valueOf(startTime));
        ps.setTimestamp(8, Timestamp.valueOf(endTime)); ps.setInt(9, sellerId);
        ps.setNull(10, Types.INTEGER); ps.setString(11, ItemStatus.PENDING.name());
        ps.setInt(12, 0); ps.setString(13, imageUrl);
        return ps.executeUpdate() > 0;
      }
    } catch (Exception e) { LOGGER.warn("insertLot failed", e); return false; }
  }

  @Override
  public void closeAuction(int itemId, int winnerId, String status) {
    String sql = "update items set winnerid = ?, status = ? where id = ?";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, winnerId); ps.setString(2, status); ps.setInt(3, itemId); ps.executeUpdate();
    } catch (Exception e) { LOGGER.warn("closeAuction failed", e); }
  }

  public boolean atomicCloseAuction(int itemId, int winnerId, String newStatus) {
    String sql = "UPDATE items SET winnerid = ?, status = ? WHERE id = ? AND status = 'OPEN'";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, winnerId); ps.setString(2, newStatus); ps.setInt(3, itemId); return ps.executeUpdate() > 0;
    } catch (Exception e) { LOGGER.warn("atomicCloseAuction failed", e); return false; }
  }

  @Override
  public boolean approveItem(int itemId) {
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(
        "update items set status = 'OPEN' where id = ? and status = 'PENDING'")) {
      ps.setInt(1, itemId); return ps.executeUpdate() > 0;
    } catch (Exception e) { LOGGER.warn("approveItem failed", e); return false; }
  }

  @Override
  public boolean rejectItem(int itemId) {
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(
        "update items set status = 'CANCELED' where id = ? and status = 'PENDING'")) {
      ps.setInt(1, itemId); return ps.executeUpdate() > 0;
    } catch (Exception e) { LOGGER.warn("rejectItem failed", e); return false; }
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
    String sql = "SELECT i.*, u.username AS seller_name, u.avatar_url AS seller_avatar FROM items i LEFT JOIN users u ON i.sellerid = u.id WHERE i.id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemId);
      try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return mapResultSetToItem(rs); }
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

  private Item mapResultSetToItem(ResultSet rs) throws SQLException {
    Item item = ItemFactory.createItem(rs.getString("category"));
    item.setId(rs.getInt("id")); item.setVersion(rs.getInt("version"));
    item.setName(rs.getString("name")); item.setDescription(rs.getString("description"));
    item.setStartingPrice(rs.getDouble("startingprice")); item.setCurrentPrice(rs.getDouble("currentprice"));
    item.setMaxPrice(rs.getDouble("maxprice"));
    Timestamp startTs = rs.getTimestamp("starttime"); if (startTs != null) item.setStartTime(startTs.toLocalDateTime());
    Timestamp endTs = rs.getTimestamp("endtime"); if (endTs != null) item.setEndTime(endTs.toLocalDateTime());
    item.setSellerId(rs.getInt("sellerid")); item.setWinnerId(rs.getInt("winnerid"));
    item.setStatus(ItemStatus.valueOf(rs.getString("status")));
    item.setImageUrl(rs.getString("image_url"));
    try {
      String sn = rs.getString("seller_name"); if (sn != null) item.setSellerUsername(sn);
      String sa = rs.getString("seller_avatar"); if (sa != null) item.setSellerAvatarUrl(sa);
    } catch (SQLException e) {}
    return item;
  }
}
