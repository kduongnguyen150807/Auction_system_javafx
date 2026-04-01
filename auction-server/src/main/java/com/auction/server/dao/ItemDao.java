package com.auction.server.dao;

import com.auction.shared.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class ItemDao {
  private Connection conn;

  public ItemDao() {
    this.conn = DatabaseConnection.getinstance().getconnection();
    this.ensureItemsImageUrlColumn();
  }

  private void ensureItemsImageUrlColumn() {
    try {
      if (!this.columnExists("items", "image_url")) {
        Statement st = this.conn.createStatement();
        st.executeUpdate("ALTER TABLE items ADD COLUMN image_url VARCHAR(2048) NULL");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private boolean columnExists(String tableName, String columnName) throws SQLException {
    String sql =
        "select 1 from information_schema.columns "
            + "where table_schema = database() and table_name = ? and column_name = ? limit 1";
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.setString(1, tableName);
    ps.setString(2, columnName);
    ResultSet rs = ps.executeQuery();
    return rs.next();
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

  public boolean insertlot(
      String title,
      String description,
      double startprice,
      java.time.LocalDateTime starttime,
      java.time.LocalDateTime endtime,
      String sellerusername,
      String imageurl,
      String cat) {
    boolean ans = false;
    try {
      int sid = -1;
      PreparedStatement ps0 = this.conn.prepareStatement("select id from users where username = ? limit 1");
      ps0.setString(1, sellerusername);
      ResultSet rs0 = ps0.executeQuery();
      if (rs0.next()) sid = rs0.getInt(1);
      if (sid <= 0) return false;
      String c = (cat == null || cat.isBlank()) ? "Vehicle" : cat.trim();
      String img = imageurl == null ? "" : imageurl.trim();
      String sql =
          "INSERT INTO items (category, name, description, startingprice, currentprice, starttime, endtime, sellerid, winnerid, status, version, image_url) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setString(1, c);
      ps.setString(2, title);
      ps.setString(3, description);
      ps.setDouble(4, startprice);
      ps.setDouble(5, startprice);
      ps.setTimestamp(6, Timestamp.valueOf(starttime));
      ps.setTimestamp(7, Timestamp.valueOf(endtime));
      ps.setInt(8, sid);
      ps.setNull(9, Types.INTEGER);
      ps.setString(10, ItemStatus.OPEN.name());
      ps.setInt(11, 0);
      if (img.isEmpty()) {
        ps.setNull(12, Types.VARCHAR);
      } else {
        ps.setString(12, img);
      }
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
    String iu = rs.getString("image_url");
    if (iu != null && !iu.isBlank()) {
      ans.setimageurl(iu);
    }
    return ans;
  }
}
