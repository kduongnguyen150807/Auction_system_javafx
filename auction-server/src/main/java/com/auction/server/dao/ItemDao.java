package com.auction.server.dao;

import com.auction.shared.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class ItemDao {
  private Connection conn;

  public ItemDao() {
    this.conn = DatabaseConnection.getinstance().getconnection();
    this.ensurecolumns();
  }

  private void ensurecolumns() {
    try {
      if (!this.columnexists("items", "image_url")) {
        Statement st = this.conn.createStatement();
        st.executeUpdate("ALTER TABLE items ADD COLUMN image_url VARCHAR(2048) NULL");
      }
      if (!this.columnexists("items", "maxprice")) {
        Statement st = this.conn.createStatement();
        st.executeUpdate("ALTER TABLE items ADD COLUMN maxprice DOUBLE NULL");
      }
    } catch (SQLException e) {}
  }

  private boolean columnexists(String res, String ans) throws SQLException {
    String res1 = "select 1 from information_schema.columns where table_schema = database() and table_name = ? and column_name = ? limit 1";
    PreparedStatement ps = this.conn.prepareStatement(res1);
    ps.setString(1, res);
    ps.setString(2, ans);
    ResultSet res2 = ps.executeQuery();
    boolean ans1 = res2.next();
    return ans1;
  }

  public List<Item> getall() {
    List<Item> ans = new ArrayList<>();
    try {
      String res = "select i.*, u.username as seller_name, u.avatar_url as seller_avatar from items i left join users u on i.sellerid = u.id";
      PreparedStatement ps = this.conn.prepareStatement(res);
      ResultSet res1 = ps.executeQuery();
      while (res1.next()) {
        ans.add(maprs(res1));
      }
    } catch (Exception e) {}
    return ans;
  }

  public Item getbyid(int res) {
    Item ans = null;
    try {
      String res1 = "select i.*, u.username as seller_name, u.avatar_url as seller_avatar from items i left join users u on i.sellerid = u.id where i.id = ?";
      PreparedStatement ps = this.conn.prepareStatement(res1);
      ps.setInt(1, res);
      ResultSet res2 = ps.executeQuery();
      if (res2.next()) {
        ans = maprs(res2);
      }
    } catch (Exception e) {}
    return ans;
  }

  public boolean updateprice(int res, double ans, int res1) {
    boolean ans1 = false;
    try {
      String res2 = "update items set currentprice = ?, version = version + 1 where id = ? and version = ?";
      PreparedStatement ps = this.conn.prepareStatement(res2);
      ps.setDouble(1, ans);
      ps.setInt(2, res);
      ps.setInt(3, res1);
      int ans2 = ps.executeUpdate();
      ans1 = ans2 > 0;
    } catch (Exception e) {}
    return ans1;
  }

  public boolean updateendtime(int res, LocalDateTime ans) {
    boolean res1 = false;
    try {
      String ans1 = "update items set endtime = ? where id = ?";
      PreparedStatement ps = this.conn.prepareStatement(ans1);
      ps.setTimestamp(1, Timestamp.valueOf(ans));
      ps.setInt(2, res);
      int res2 = ps.executeUpdate();
      res1 = res2 > 0;
    } catch (Exception e) {}
    return res1;
  }

  public boolean insertlot(String res, String ans, double res1, double ans1, LocalDateTime res2, LocalDateTime ans2, String res3, String ans3, String res4) {
    boolean ans4 = false;
    try {
      int res5 = -1;
      PreparedStatement ps0 = this.conn.prepareStatement("select id from users where username = ? limit 1");
      ps0.setString(1, res3);
      ResultSet rs0 = ps0.executeQuery();
      if (rs0.next()) res5 = rs0.getInt(1);
      if (res5 <= 0) return false;

      String res6 = "INSERT INTO items (category, name, description, startingprice, currentprice, maxprice, starttime, endtime, sellerid, winnerid, status, version, image_url) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      PreparedStatement ps = this.conn.prepareStatement(res6);
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
    } catch (Exception e) {}
    return ans4;
  }

  public void closeauction(int res, int ans, String res1) {
    try {
      String ans1 = "update items set winnerid = ?, status = ? where id = ?";
      PreparedStatement ps = this.conn.prepareStatement(ans1);
      ps.setInt(1, ans);
      ps.setString(2, res1);
      ps.setInt(3, res);
      ps.executeUpdate();
    } catch (Exception e) {}
  }

  private Item maprs(ResultSet res) throws SQLException {
    String ans = res.getString("category");
    Item res1 = ItemFactory.createitem(ans);
    res1.setid(res.getInt("id"));
    res1.setversion(res.getInt("version"));
    res1.setname(res.getString("name"));
    res1.setdescription(res.getString("description"));
    res1.setstartingprice(res.getDouble("startingprice"));
    res1.setcurrentprice(res.getDouble("currentprice"));
    res1.setmaxprice(res.getDouble("maxprice"));

    Timestamp ans1 = res.getTimestamp("starttime");
    if (ans1 != null) res1.setstarttime(ans1.toLocalDateTime());
    Timestamp res2 = res.getTimestamp("endtime");
    if (res2 != null) res1.setendtime(res2.toLocalDateTime());
    res1.setsellerid(res.getInt("sellerid"));
    res1.setwinnerid(res.getInt("winnerid"));
    res1.setstatus(ItemStatus.valueOf(res.getString("status")));
    res1.setimageurl(res.getString("image_url"));
    try {
      String ans2 = res.getString("seller_name");
      if (ans2 != null) res1.setsellerusername(ans2);
      String res3 = res.getString("seller_avatar");
      if (res3 != null) res1.setselleravatarurl(res3);
    } catch (SQLException e) {}
    return res1;
  }

  public java.util.List<Item> getexpireditems() {
    java.util.List<Item> ans = new java.util.ArrayList<>();
    try {
      String res = "SELECT * FROM items WHERE endtime <= NOW() AND status = 'OPEN'";
      java.sql.PreparedStatement ps = this.conn.prepareStatement(res);
      java.sql.ResultSet res1 = ps.executeQuery();
      while (res1.next()) {
        Item ans1 = maprs(res1);
        ans.add(ans1);
      }
    } catch (Exception e) {}
    return ans;
  }

  public java.util.List<Item> getbysellerid(int res) {
    java.util.List<Item> ans = new java.util.ArrayList<>();
    try {
      String res1 = "SELECT * FROM items WHERE sellerid = ? ORDER BY id DESC";
      java.sql.PreparedStatement ps = this.conn.prepareStatement(res1);
      ps.setInt(1, res);
      java.sql.ResultSet ans1 = ps.executeQuery();
      while (ans1.next()) {
        Item res2 = maprs(ans1);
        ans.add(res2);
      }
    } catch (Exception e) {}
    return ans;
  }

  public java.util.List<Item> getpendingitems() {
    java.util.List<Item> ans = new java.util.ArrayList<>();
    try {
      String res = "select i.*, u.username as seller_name, u.avatar_url as seller_avatar from items i left join users u on i.sellerid = u.id where i.status = 'PENDING' order by i.id desc";
      PreparedStatement ps = this.conn.prepareStatement(res);
      ResultSet res1 = ps.executeQuery();
      while (res1.next()) {
        ans.add(maprs(res1));
      }
    } catch (Exception e) {}
    return ans;
  }

  public boolean approveitem(int res) {
    boolean ans = false;
    try {
      String res1 = "update items set status = 'OPEN' where id = ? and status = 'PENDING'";
      PreparedStatement ps = this.conn.prepareStatement(res1);
      ps.setInt(1, res);
      int ans1 = ps.executeUpdate();
      ans = ans1 > 0;
    } catch (Exception e) {}
    return ans;
  }

  public boolean rejectitem(int res) {
    boolean ans = false;
    try {
      String res1 = "update items set status = 'CANCELED' where id = ? and status = 'PENDING'";
      PreparedStatement ps = this.conn.prepareStatement(res1);
      ps.setInt(1, res);
      int ans1 = ps.executeUpdate();
      ans = ans1 > 0;
    } catch (Exception e) {}
    return ans;
  }

  public java.util.HashMap<String, Integer> getstatusstats() {
    java.util.HashMap<String, Integer> ans = new java.util.HashMap<>();
    try {
      String res = "select status, count(*) as cnt from items group by status";
      PreparedStatement ps = this.conn.prepareStatement(res);
      ResultSet res1 = ps.executeQuery();
      while (res1.next()) {
        ans.put(res1.getString("status"), res1.getInt("cnt"));
      }
    } catch (Exception e) {}
    return ans;
  }

  public java.util.HashMap<String, Double> getcategorystats() {
    java.util.HashMap<String, Double> ans = new java.util.HashMap<>();
    try {
      String res = "select category, sum(currentprice) as total from items where status in ('CLOSED', 'FINISHED') group by category";
      PreparedStatement ps = this.conn.prepareStatement(res);
      ResultSet res1 = ps.executeQuery();
      while (res1.next()) {
        ans.put(res1.getString("category"), res1.getDouble("total"));
      }
    } catch (Exception e) {}
    return ans;
  }
}