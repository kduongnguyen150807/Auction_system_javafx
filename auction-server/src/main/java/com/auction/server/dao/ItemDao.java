package com.auction.server.dao;

import com.auction.shared.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class ItemDao {
  public ItemDao() {}

  public List<Item> getAll() {
    List<Item> ans = new ArrayList<>();
    String sql =
            "select i.*, u.username as seller_name, u.avatar_url as seller_avatar from items i left join users u on i.sellerid = u.id";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
        try (ResultSet res1 = ps.executeQuery()) {
            while (res1.next()) {
                ans.add(mapResultSet(res1));
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ans;
  }

  public Item getById(int res) {
    Item ans = null;
    String sql =
            "select i.*, u.username as seller_name, u.avatar_url as seller_avatar from items i left join users u on i.sellerid = u.id where i.id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, res);
        try (ResultSet res2 = ps.executeQuery()) {
            if (res2.next()) {
                ans = mapResultSet(res2);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ans;
  }

    public boolean updatePrice(int id, double price, int version) {
        String sql = "update items set currentprice = ?, version = version + 1 where id = ? and version = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, price);
            ps.setInt(2, id);
            ps.setInt(3, version);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updatePrice(Connection conn, int id, double price, int version) {
        String sql = "update items set currentprice = ?, version = version + 1 where id = ? and version = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, price);
            ps.setInt(2, id);
            ps.setInt(3, version);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateEndTime(Connection conn, int id, java.time.LocalDateTime newEndTime) {
        String sql = "update items set endtime = ? where id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(newEndTime));
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

  public boolean insertLot(
      String res,
      String ans,
      double res1,
      double ans1,
      LocalDateTime res2,
      LocalDateTime ans2,
      String res3,
      String ans3,
      String res4) {
    boolean ans4 = false;
    String sql = "select id from users where username = ? limit 1";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement ps0 = conn.prepareStatement(sql)){
        int res5 = -1;
        ps0.setString(1, res3);
        try (ResultSet rs0 = ps0.executeQuery()) {
            if (rs0.next()) res5 = rs0.getInt(1);
            if (res5 <= 0) return false;
        }

        String res6 =
            "INSERT INTO items (category, name, description, startingprice, currentprice, maxprice, starttime, endtime, sellerid, winnerid, status, version, image_url) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try(PreparedStatement ps = conn.prepareStatement(res6)) {
            ps.setString(1, res4 == null ? "Vehicle" : res4);
            ps.setString(2, res);
            ps.setString(3, ans);
            ps.setDouble(4, res1);
            ps.setDouble(5, res1);
            ps.setDouble(6, ans1);
            ps.setTimestamp(7, Timestamp.valueOf(res2));
            ps.setTimestamp(8, Timestamp.valueOf(ans2));
            ps.setInt(9, res5);
            ps.setNull(10, Types.INTEGER);
            ps.setString(11, ItemStatus.PENDING.name());
            ps.setInt(12, 0);
            ps.setString(13, ans3);
            int res7 = ps.executeUpdate();
            ans4 = res7 > 0;
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ans4;
  }

  public void closeAuction(int res, int ans, String res1) {
    String sql = "update items set winnerid = ?, status = ? where id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, ans);
      ps.setString(2, res1);
      ps.setInt(3, res);
      ps.executeUpdate();
    } catch (Exception e) {
        e.printStackTrace();
    }
  }
    public void closeAuction(Connection conn, int id, int winnerId, String status) {
        String sql = "update items set winnerid = ?, status = ? where id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, winnerId);
            ps.setString(2, status);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

  private Item mapResultSet(ResultSet res) throws SQLException {
    String ans = res.getString("category");
    Item res1 = ItemFactory.createItem(ans);
    res1.setId(res.getInt("id"));
    res1.setVersion(res.getInt("version"));
    res1.setName(res.getString("name"));
    res1.setDescription(res.getString("description"));
    res1.setStartingPrice(res.getDouble("startingprice"));
    res1.setCurrentPrice(res.getDouble("currentprice"));
    res1.setMaxPrice(res.getDouble("maxprice"));

    Timestamp ans1 = res.getTimestamp("starttime");
    if (ans1 != null) res1.setStartTime(ans1.toLocalDateTime());
    Timestamp res2 = res.getTimestamp("endtime");
    if (res2 != null) res1.setEndTime(res2.toLocalDateTime());
    res1.setSellerId(res.getInt("sellerid"));
    res1.setWinnerId(res.getInt("winnerid"));
    res1.setStatus(ItemStatus.valueOf(res.getString("status")));
    res1.setImageUrl(res.getString("image_url"));
    try {
      String ans2 = res.getString("seller_name");
      if (ans2 != null) res1.setSellerUsername(ans2);
      String res3 = res.getString("seller_avatar");
      if (res3 != null) res1.setSellerAvatarUrl(res3);
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return res1;
  }

  public java.util.List<Item> getExpiredItems() {
    java.util.List<Item> ans = new java.util.ArrayList<>();
    String sql = "SELECT * FROM items WHERE endtime <= NOW() AND status = 'OPEN'";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
        try (ResultSet res1 = ps.executeQuery()){
          while (res1.next()) {
            Item ans1 = mapResultSet(res1);
            ans.add(ans1);
          }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ans;
  }

  public java.util.List<Item> getBySellerId(int res) {
    java.util.List<Item> ans = new java.util.ArrayList<>();
    String sql = "SELECT * FROM items WHERE sellerid = ? ORDER BY id DESC";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, res);
      try (ResultSet ans1 = ps.executeQuery()) {
        while (ans1.next()) {
          Item res2 = mapResultSet(ans1);
          ans.add(res2);
        }
      }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ans;
  }

  public java.util.List<Item> getPendingItems() {
    java.util.List<Item> ans = new java.util.ArrayList<>();
    String sql =
            "select i.*, u.username as seller_name, u.avatar_url as seller_avatar from items i left join users u on i.sellerid = u.id where i.status = 'PENDING' order by i.id desc";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
        try (ResultSet res1 = ps.executeQuery()) {
          while (res1.next()) {
            ans.add(mapResultSet(res1));
          }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ans;
  }

  public boolean approveItem(int res) {
    boolean ans = false;
    String sql = "update items set status = 'OPEN' where id = ? and status = 'PENDING'";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, res);
        int ans1 = ps.executeUpdate();
        ans = ans1 > 0;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ans;
  }

  public boolean rejectItem(int res) {
    boolean ans = false;
    String sql = "update items set status = 'CANCELED' where id = ? and status = 'PENDING'";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, res);
        int ans1 = ps.executeUpdate();
        ans = ans1 > 0;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ans;
  }

  public java.util.HashMap<String, Integer> getStatusStats() {
    java.util.HashMap<String, Integer> ans = new java.util.HashMap<>();
    String sql = "select status, count(*) as cnt from items group by status";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
        try (ResultSet res1 = ps.executeQuery()) {
          while (res1.next()) {
            ans.put(res1.getString("status"), res1.getInt("cnt"));
          }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ans;
  }

  public java.util.HashMap<String, Double> getCategoryStats() {
    java.util.HashMap<String, Double> ans = new java.util.HashMap<>();
    String sql =
            "select category, sum(currentprice) as total from items where status in ('CLOSED', 'FINISHED') group by category";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
        try (ResultSet res1 = ps.executeQuery()) {
          while (res1.next()) {
            ans.put(res1.getString("category"), res1.getDouble("total"));
          }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ans;
  }
}
