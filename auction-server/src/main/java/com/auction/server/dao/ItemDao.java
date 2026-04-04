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
      // Đảm bảo cột image_url tồn tại
      if (!this.columnexists("items", "image_url")) {
        Statement st = this.conn.createStatement();
        st.executeUpdate("ALTER TABLE items ADD COLUMN image_url VARCHAR(2048) NULL");
      }
      // Thêm cột maxprice cho tính năng mua đứt
      if (!this.columnexists("items", "maxprice")) {
        Statement st = this.conn.createStatement();
        st.executeUpdate("ALTER TABLE items ADD COLUMN maxprice DOUBLE NULL");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private boolean columnexists(String tableName, String columnName) throws SQLException {
    String sql = "select 1 from information_schema.columns where table_schema = database() and table_name = ? and column_name = ? limit 1";
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.setString(1, tableName);
    ps.setString(2, columnName);
    ResultSet rs = ps.executeQuery();
    boolean ans = rs.next();
    return ans;
  }

  public List<Item> getall() {
    List<Item> ans = new ArrayList<>();
    try {
      // Truy vấn lấy thông tin sản phẩm và thông tin người bán
      String sql = "select i.*, u.username as seller_name, u.avatar_url as seller_avatar from items i left join users u on i.sellerid = u.id";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        ans.add(maprs(rs));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public Item getbyid(int id) {
    Item ans = null;
    try {
      String sql = "select i.*, u.username as seller_name, u.avatar_url as seller_avatar from items i left join users u on i.sellerid = u.id where i.id = ?";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        ans = maprs(rs);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public boolean updateprice(int id, double v, int ver) {
    boolean ans = false;
    try {
      String sql = "update items set currentprice = ?, version = version + 1 where id = ? and version = ?";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setDouble(1, v);
      ps.setInt(2, id);
      ps.setInt(3, ver);
      int res = ps.executeUpdate();
      ans = res > 0;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public boolean updateendtime(int id, LocalDateTime t) {
    boolean ans = false;
    try {
      String sql = "update items set endtime = ? where id = ?";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setTimestamp(1, Timestamp.valueOf(t));
      ps.setInt(2, id);
      int res = ps.executeUpdate();
      ans = res > 0;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  // Cập nhật phương thức insertlot để nhận tham số maxp
  public boolean insertlot(String n, String d, double sp, double maxp, LocalDateTime st, LocalDateTime et, String sname, String url, String cat) {
    boolean ans = false;
    try {
      int sid = -1;
      PreparedStatement ps0 = this.conn.prepareStatement("select id from users where username = ? limit 1");
      ps0.setString(1, sname);
      ResultSet rs0 = ps0.executeQuery();
      if (rs0.next()) sid = rs0.getInt(1);
      if (sid <= 0) return false;

      String sql = "INSERT INTO items (category, name, description, startingprice, currentprice, maxprice, starttime, endtime, sellerid, winnerid, status, version, image_url) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setString(1, cat == null ? "Vehicle" : cat);
      ps.setString(2, n);
      ps.setString(3, d);
      ps.setDouble(4, sp);
      ps.setDouble(5, sp);
      ps.setDouble(6, maxp);
      ps.setTimestamp(7, Timestamp.valueOf(st));
      ps.setTimestamp(8, Timestamp.valueOf(et));
      ps.setInt(9, sid);
      ps.setNull(10, Types.INTEGER);
      ps.setString(11, ItemStatus.PENDING.name());
      ps.setInt(12, 0);
      ps.setString(13, url);
      int res = ps.executeUpdate();
      ans = res > 0;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public void closeauction(int id, int wid, String s) {
    try {
      String sql = "update items set winnerid = ?, status = ? where id = ?";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setInt(1, wid);
      ps.setString(2, s);
      ps.setInt(3, id);
      ps.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Item maprs(ResultSet rs) throws SQLException {
    String c = rs.getString("category");
    Item ans = (c != null && c.equalsIgnoreCase("Electronics")) ? new Electronics() : (c != null && c.equalsIgnoreCase("Art") ? new Art() : new Vehicle());
    ans.setid(rs.getInt("id"));
    ans.setversion(rs.getInt("version"));
    ans.setname(rs.getString("name"));
    ans.setdescription(rs.getString("description"));
    ans.setstartingprice(rs.getDouble("startingprice"));
    ans.setcurrentprice(rs.getDouble("currentprice"));
    // Ánh xạ maxprice từ ResultSet vào đối tượng Item
    ans.setmaxprice(rs.getDouble("maxprice"));

    Timestamp st = rs.getTimestamp("starttime");
    if (st != null) ans.setstarttime(st.toLocalDateTime());
    Timestamp et = rs.getTimestamp("endtime");
    if (et != null) ans.setendtime(et.toLocalDateTime());
    ans.setsellerid(rs.getInt("sellerid"));
    ans.setwinnerid(rs.getInt("winnerid"));
    ans.setstatus(ItemStatus.valueOf(rs.getString("status")));
    ans.setimageurl(rs.getString("image_url"));
    try {
      String name = rs.getString("seller_name");
      if (name != null) ans.setsellerusername(name);
      String avatar = rs.getString("seller_avatar");
      if (avatar != null) ans.setselleravatarurl(avatar);
    } catch (SQLException e) {
    }
    return ans;
  }
  public java.util.List<Item> getexpireditems() {
    java.util.List<Item> ans = new java.util.ArrayList<>();
    try {
      String res = "SELECT * FROM items WHERE endtime <= NOW() AND status = 'OPEN'";
      java.sql.PreparedStatement ps = this.conn.prepareStatement(res);
      java.sql.ResultSet res1 = ps.executeQuery();
      while (res1.next()) {
        // Dùng hàm maprs mày đã viết sẵn để bốc data lên
        Item res2 = maprs(res1);
        ans.add(res2);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }
  public java.util.List<Item> getbysellerid(int sid) {
    java.util.List<Item> ans = new java.util.ArrayList<>();
    try {
      // Đổi createdat thành id để sắp xếp, vì bảng items của mày có id chắc luôn
      String res = "SELECT * FROM items WHERE sellerid = ? ORDER BY id DESC";
      java.sql.PreparedStatement ps = this.conn.prepareStatement(res);
      ps.setInt(1, sid);
      java.sql.ResultSet res1 = ps.executeQuery();
      while (res1.next()) {
        Item res2 = maprs(res1);
        ans.add(res2);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public boolean approveitem(int id) {
    boolean ans = false;
    try {
      String res = "update items set status = 'OPEN' where id = ? and status = 'PENDING'";
      PreparedStatement ps = this.conn.prepareStatement(res);
      ps.setInt(1, id);
      int res1 = ps.executeUpdate();
      ans = res1 > 0;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public boolean rejectitem(int id) {
    boolean ans = false;
    try {
      String res = "update items set status = 'CANCELED' where id = ? and status = 'PENDING'";
      PreparedStatement ps = this.conn.prepareStatement(res);
      ps.setInt(1, id);
      int res1 = ps.executeUpdate();
      ans = res1 > 0;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }
}