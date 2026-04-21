package com.auction.server.dao;

import com.auction.shared.*;
import java.sql.*;
import java.util.*;

public class BidDao {

  public BidDao() {
  }

  public boolean placeBid(BidTransaction b) {
    String sql1 = "insert into bid_transactions(itemid,userid,bidvalue,timestamp) values(?,?,?,?)";
    String sql2 = "update items set currentprice = ? where id = ?";

    try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
      conn.setAutoCommit(false);

      try (PreparedStatement ps1 = conn.prepareStatement(sql1);
           PreparedStatement ps2 = conn.prepareStatement(sql2)) {

        ps1.setInt(1, b.getItemId());
        ps1.setInt(2, b.getUserId());
        ps1.setDouble(3, b.getBidValue());
        ps1.setTimestamp(4, Timestamp.valueOf(b.getTimestamp()));

        int res = ps1.executeUpdate();
        if (res <= 0) {
          conn.rollback();
          return false;
        }

        ps2.setDouble(1, b.getBidValue());
        ps2.setInt(2, b.getItemId());
        ps2.executeUpdate();

        conn.commit();
        return true;

      } catch (Exception e) {
        conn.rollback();
        e.printStackTrace();
        return false;
      } finally {
        conn.setAutoCommit(true);
      }

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean addBid(BidTransaction b) {
    boolean ans = false;
    String sql = "insert into bid_transactions(itemid,userid,bidvalue,timestamp) values(?,?,?,?)";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
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
    String sql = "select * from bid_transactions where itemid = ? order by timestamp asc";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
        ps.setInt(1, iid);
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            BidTransaction b =
                    new BidTransaction(rs.getInt("itemid"), rs.getInt("userid"), rs.getDouble("bidvalue"));
            b.setId(rs.getInt("id"));
            b.setVersion(rs.getInt("version"));
            b.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            ans.add(b);
          }
        }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public BidTransaction getWinner(int iid) {
    BidTransaction ans = null;
    String sql =
            "select * from bid_transactions where itemid = ? order by bidvalue desc, timestamp asc limit 1";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
        ps.setInt(1, iid);
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            ans =
                    new BidTransaction(rs.getInt("itemid"), rs.getInt("userid"), rs.getDouble("bidvalue"));
            ans.setId(rs.getInt("id"));
            ans.setVersion(rs.getInt("version"));
            ans.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
          }
        }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }
}
