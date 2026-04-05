package com.auction.server.dao;

import com.auction.shared.*;
import java.sql.*;
import java.util.*;

public class BidDao {
  private Connection conn;

  public BidDao() {
    this.conn = DatabaseConnection.getInstance().getConnection();
  }

  public boolean placeBid(BidTransaction b) {
    boolean ans = false;
    try {
      this.conn.setAutoCommit(false);
      String sql = "insert into bid_transactions(itemid,userid,bidvalue,timestamp) values(?,?,?,?)";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setInt(1, b.getItemId());
      ps.setInt(2, b.getUserId());
      ps.setDouble(3, b.getBidValue());
      ps.setTimestamp(4, Timestamp.valueOf(b.getTimestamp()));
      int res = ps.executeUpdate();
      if (res > 0) {
        String sql2 = "update items set currentprice = ? where id = ?";
        PreparedStatement ps2 = this.conn.prepareStatement(sql2);
        ps2.setDouble(1, b.getBidValue());
        ps2.setInt(2, b.getItemId());
        ps2.executeUpdate();
      }
      this.conn.commit();
      ans = res > 0;
    } catch (Exception e) {
      try {
        this.conn.rollback();
      } catch (Exception e2) {
        e2.printStackTrace();
      }
      e.printStackTrace();
    } finally {
      try {
        this.conn.setAutoCommit(true);
      } catch (Exception e3) {
        e3.printStackTrace();
      }
    }
    return ans;
  }

  public boolean addBid(BidTransaction b) {
    boolean ans = false;
    try {
      String sql = "insert into bid_transactions(itemid,userid,bidvalue,timestamp) values(?,?,?,?)";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setInt(1, b.getItemId());
      ps.setInt(2, b.getUserId());
      ps.setDouble(3, b.getBidValue());
      ps.setTimestamp(4, Timestamp.valueOf(b.getTimestamp()));
      ans = ps.executeUpdate() > 0;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public List<BidTransaction> getByItem(int iid) {
    List<BidTransaction> ans = new ArrayList<>();
    try {
      String sql = "select * from bid_transactions where itemid = ? order by timestamp asc";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setInt(1, iid);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        BidTransaction b =
            new BidTransaction(rs.getInt("itemid"), rs.getInt("userid"), rs.getDouble("bidvalue"));
        b.setId(rs.getInt("id"));
        b.setVersion(rs.getInt("version"));
        b.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        ans.add(b);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public BidTransaction getWinner(int iid) {
    BidTransaction ans = null;
    try {
      String sql =
          "select * from bid_transactions where itemid = ? order by bidvalue desc, timestamp asc limit 1";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setInt(1, iid);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        ans =
            new BidTransaction(rs.getInt("itemid"), rs.getInt("userid"), rs.getDouble("bidvalue"));
        ans.setId(rs.getInt("id"));
        ans.setVersion(rs.getInt("version"));
        ans.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }
}
