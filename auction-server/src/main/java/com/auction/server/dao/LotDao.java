package com.auction.server.dao;

import com.auction.shared.Lot;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LotDao {
  private Connection conn;

  public LotDao() {
    this.conn = DatabaseConnection.getInstance().getConnection();
  }

  public List<Lot> getOngoingBids(int userId) {
    List<Lot> ans = new ArrayList<>();
    try {
      String sql =
          "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar "
              + "FROM items i "
              + "LEFT JOIN users u ON i.sellerid = u.id "
              + "WHERE i.status = 'OPEN' AND i.starttime <= NOW() AND i.endtime > NOW()";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        ans.add(mapResultSet(rs));
      }
    } catch (Exception e) {
    }
    return ans;
  }

  public List<Lot> getUpcomingBids(int userId) {
    List<Lot> ans = new ArrayList<>();
    try {
      String sql =
          "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar "
              + "FROM items i "
              + "LEFT JOIN users u ON i.sellerid = u.id "
              + "WHERE i.status = 'OPEN' AND i.starttime > NOW()";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        ans.add(mapResultSet(rs));
      }
    } catch (Exception e) {
    }
    return ans;
  }

  public List<Lot> getClosedBids(int userId) {
    List<Lot> ans = new ArrayList<>();
    try {
      String sql =
          "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar, w.username as w_name "
              + "FROM items i "
              + "LEFT JOIN users u ON i.sellerid = u.id "
              + "LEFT JOIN users w ON i.winnerid = w.id "
              + "WHERE i.status = 'CLOSED'";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        ans.add(mapResultSet(rs));
      }
    } catch (Exception e) {
    }
    return ans;
  }

  public List<Lot> getPastBids(int userId) {
    List<Lot> ans = new ArrayList<>();
    try {
      String sql =
          "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar, w.username as w_name "
              + "FROM items i "
              + "LEFT JOIN users u ON i.sellerid = u.id "
              + "LEFT JOIN users w ON i.winnerid = w.id "
              + "WHERE i.status IN ('FINISHED', 'CANCELED') OR (i.status = 'OPEN' AND i.endtime <= NOW())";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        ans.add(mapResultSet(rs));
      }
    } catch (Exception e) {
    }
    return ans;
  }

  private Lot mapResultSet(ResultSet rs) throws SQLException {
    Lot ans = new Lot();
    ans.setId(rs.getInt("id"));
    ans.setTitle(rs.getString("name"));
    ans.setDescription(rs.getString("description"));
    ans.setBidValue(rs.getDouble("currentprice"));
    ans.setStartTime(rs.getTimestamp("starttime").toLocalDateTime());
    ans.setEndTime(rs.getTimestamp("endtime").toLocalDateTime());
    ans.setImageUrl(rs.getString("image_url"));
    ans.setSellerUsername(rs.getString("s_name"));
    ans.setSellerAvatarUrl(rs.getString("s_avatar"));
    try {
      String wname = rs.getString("w_name");
      if (wname != null) ans.setWinnerUsername(wname);
    } catch (Exception e) {
    }
    return ans;
  }
}
