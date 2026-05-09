package com.auction.server.dao.auction;

import com.auction.server.dao.platform.BaseDao;
import com.auction.shared.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemDao extends BaseDao<Item> implements ItemRepository {
  private static final String join_seller =
          "select i.*, u.username as seller_name, u.avatar_url as seller_avatar from items i left join users u on i.sellerid = u.id";

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
    Timestamp startts = rs.getTimestamp("starttime");
    if (startts != null) {
      item.setStartTime(startts.toLocalDateTime());
    }
    Timestamp endts = rs.getTimestamp("endtime");
    if (endts != null) {
      item.setEndTime(endts.toLocalDateTime());
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
    } catch (SQLException e) {
    }
    try {
      String sn = rs.getString("seller_name");
      if (sn != null) {
        item.setSellerUsername(sn);
      }
      String sa = rs.getString("seller_avatar");
      if (sa != null) {
        item.setSellerAvatarUrl(sa);
      }
    } catch (SQLException e) {
    }
    return item;
  }

  @Override
  public List<Item> getAll() {
    List<Item> ans = queryList(join_seller);
    return ans;
  }

  @Override
  public Item getById(int itemid) {
    Item ans = querySingle(join_seller + " where i.id = ?", itemid);
    return ans;
  }

  @Override
  public List<Item> getBySellerId(int sellerid) {
    List<Item> ans = queryList(join_seller + " where i.sellerid = ? order by i.id desc", sellerid);
    return ans;
  }

  @Override
  public List<Item> getExpiredItems() {
    List<Item> ans = queryList("select * from items where endtime <= now() and status = 'OPEN'");
    return ans;
  }

  @Override
  public List<Item> getPendingItems() {
    List<Item> ans = queryList(join_seller + " where i.status = 'PENDING' order by i.id desc");
    return ans;
  }

  @Override
  public boolean updatePrice(int itemid, double price, int version) {
    boolean ans = executeUpdate("update items set currentprice = ?, version = version + 1 where id = ? and version = ?", price, itemid, version);
    return ans;
  }

  @Override
  public boolean updateEndTime(int itemid, LocalDateTime endtime) {
    boolean ans = executeUpdate("update items set endtime = ? where id = ?", Timestamp.valueOf(endtime), itemid);
    return ans;
  }

  @Override
  public boolean insertLot(String name, String description, double startingprice, double maxprice, LocalDateTime starttime, LocalDateTime endtime, String sellerusername, String imageurl, String category, AuctionType auctiontype, double dutchreserveprice, double dutchtickamount, int dutchtickintervalminutes) {
    try (Connection conn = getConn()) {
      int sellerid = -1;
      try (PreparedStatement lookup = conn.prepareStatement("select id from users where username = ? limit 1")) {
        lookup.setString(1, sellerusername);
        ResultSet rs = lookup.executeQuery();
        if (rs.next()) {
          sellerid = rs.getInt(1);
        }
      }
      if (sellerid <= 0) {
        return false;
      }
      AuctionType kind = auctiontype != null ? auctiontype : AuctionType.ENGLISH;
      String sql = "insert into items (category, auction_type, name, description, startingprice, currentprice, maxprice, dutch_reserve_price, dutch_tick_amount, dutch_tick_interval_mins, starttime, endtime, sellerid, winnerid, status, version, image_url) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, category == null ? "Vehicle" : category);
        ps.setString(2, kind.dbName());
        ps.setString(3, name);
        ps.setString(4, description);
        ps.setDouble(5, startingprice);
        ps.setDouble(6, startingprice);
        ps.setDouble(7, maxprice);
        if (kind == AuctionType.DUTCH) {
          ps.setDouble(8, dutchreserveprice);
          ps.setDouble(9, dutchtickamount);
          ps.setInt(10, dutchtickintervalminutes);
        } else {
          ps.setNull(8, Types.DOUBLE);
          ps.setNull(9, Types.DOUBLE);
          ps.setNull(10, Types.INTEGER);
        }
        ps.setTimestamp(11, Timestamp.valueOf(starttime));
        ps.setTimestamp(12, Timestamp.valueOf(endtime));
        ps.setInt(13, sellerid);
        ps.setNull(14, Types.INTEGER);
        ps.setString(15, ItemStatus.PENDING.name());
        ps.setInt(16, 0);
        ps.setString(17, imageurl);
        boolean ans = ps.executeUpdate() > 0;
        return ans;
      }
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void closeAuction(int itemid, int winnerid, String status) {
    executeUpdate("update items set winnerid = ?, status = ? where id = ?", winnerid, status, itemid);
  }

  public boolean atomicCloseAuction(int itemid, int winnerid, String newstatus) {
    boolean ans = executeUpdate("update items set winnerid = ?, status = ? where id = ? and status = 'OPEN'", winnerid, newstatus, itemid);
    return ans;
  }

  @Override
  public boolean approveItem(int itemid) {
    boolean ans = executeUpdate("update items set status = 'OPEN' where id = ? and status = 'PENDING'", itemid);
    return ans;
  }

  @Override
  public boolean rejectItem(int itemid) {
    boolean ans = executeUpdate("update items set status = 'CANCELED' where id = ? and status = 'PENDING'", itemid);
    return ans;
  }

  @Override
  public HashMap<String, Integer> getStatusStats() {
    HashMap<String, Integer> ans = new HashMap<>();
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement("select status, count(*) as cnt from items group by status")) {
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        ans.put(rs.getString("status"), rs.getInt("cnt"));
      }
    } catch (Exception e) {
    }
    return ans;
  }

  @Override
  public HashMap<String, Double> getCategoryStats() {
    HashMap<String, Double> ans = new HashMap<>();
    String sql = "select category, sum(currentprice) as total from items where status in ('CLOSED', 'FINISHED') group by category";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        ans.put(rs.getString("category"), rs.getDouble("total"));
      }
    } catch (Exception e) {
    }
    return ans;
  }

  public List<Integer> getOpenAuctionIdsBySeller(int sellerid) {
    List<Integer> ans = new ArrayList<>();
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement("select id from items where sellerid = ? and status = 'OPEN'")) {
      ps.setInt(1, sellerid);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        ans.add(rs.getInt(1));
      }
    } catch (Exception e) {
    }
    return ans;
  }

  public Item getByIdTx(int itemid, Connection conn) throws SQLException {
    String sql = "select i.*, u.username as seller_name, u.avatar_url as seller_avatar from items i left join users u on i.sellerid = u.id where i.id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemid);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Item ans = mapRow(rs);
          return ans;
        }
      }
    }
    return null;
  }

  public boolean cancelAuctionTx(int itemid, Connection conn) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("update items set status = 'CANCELED' where id = ? and status = 'OPEN'")) {
      ps.setInt(1, itemid);
      boolean ans = ps.executeUpdate() > 0;
      return ans;
    }
  }

  public boolean updatePriceTx(int itemid, double price, Connection conn) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("update items set currentprice = ?, version = version + 1 where id = ?")) {
      ps.setDouble(1, price);
      ps.setInt(2, itemid);
      boolean ans = ps.executeUpdate() > 0;
      return ans;
    }
  }
}