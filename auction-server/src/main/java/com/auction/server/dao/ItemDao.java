package com.auction.server.dao;

import com.auction.shared.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class ItemDao {
  private Connection conn;

  public ItemDao() {
    this.conn = DatabaseConnection.getinstance().getconnection();
  }

  public List<Item> getall() {
    List<Item> ans = new ArrayList<>();
    try {
      String sql = "select * from items";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) ans.add(maprs(rs));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public Item getbyid(int id) {
    Item ans = null;
    try {
      String sql = "select * from items where id = ?";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) ans = maprs(rs);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public boolean updateprice(int id, double val, int currentversion) {
    boolean ans = false;
    try {
      String sql =
          "update items set currentprice = ?, version = version + 1 where id = ? and version = ?";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setDouble(1, val);
      ps.setInt(2, id);
      ps.setInt(3, currentversion);
      ans = ps.executeUpdate() > 0;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public boolean updateendtime(int id, LocalDateTime time) {
    boolean ans = false;
    try {
      String sql = "update items set endtime = ? where id = ?";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setTimestamp(1, Timestamp.valueOf(time));
      ps.setInt(2, id);
      ans = ps.executeUpdate() > 0;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public boolean insertlot(String title, String description, double startprice, java.time.LocalDateTime endtime, String sellerusername, String imageurl) {
    boolean ans = false;
    try {
      String sql = "INSERT INTO lots (title, description, start_price, end_time, seller_username, image_url) VALUES (?, ?, ?, ?, ?, ?)";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setString(1, title);
      ps.setString(2, description);
      ps.setDouble(3, startprice);
      ps.setTimestamp(4, Timestamp.valueOf(endtime));
      ps.setString(5, sellerusername);
      ps.setString(6, imageurl);
      int res = ps.executeUpdate();
      ans = res > 0;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }
  public void closeauction(int id, int winnerid, String status) {
    try {
      String sql = "update items set winnerid = ?, status = ? where id = ?";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setInt(1, winnerid);
      ps.setString(2, status);
      ps.setInt(3, id);
      ps.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Item maprs(ResultSet rs) throws SQLException {
    String cat = rs.getString("category");
    Item ans = null;
    if (cat.equalsIgnoreCase("Electronics")) ans = new Electronics();
    else if (cat.equalsIgnoreCase("Art")) ans = new Art();
    else ans = new Vehicle();
    ans.setid(rs.getInt("id"));
    ans.setversion(rs.getInt("version"));
    ans.setname(rs.getString("name"));
    ans.setdescription(rs.getString("description"));
    ans.setstartingprice(rs.getDouble("startingprice"));
    ans.setcurrentprice(rs.getDouble("currentprice"));
    ans.setstarttime(rs.getTimestamp("starttime").toLocalDateTime());
    ans.setendtime(rs.getTimestamp("endtime").toLocalDateTime());
    ans.setsellerid(rs.getInt("sellerid"));
    ans.setwinnerid(rs.getInt("winnerid"));
    ans.setstatus(ItemStatus.valueOf(rs.getString("status")));
    return ans;
  }
}
