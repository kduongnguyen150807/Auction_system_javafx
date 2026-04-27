package com.auction.server.dao;

import com.auction.shared.BidTransaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BidDao {
  private final Connection conn;

  public BidDao() {
    this.conn = null;
  }

  public BidDao(Connection conn) {
    this.conn = conn;
  }

  private Connection getConnection() throws SQLException {
    if (this.conn != null) {
      return this.conn;
    }
    return DatabaseConnection.getInstance().getConnection();
  }

  public boolean insertBid(BidTransaction b) {
    String sql = "insert into bid_transactions(itemid,userid,bidvalue,timestamp) values(?,?,?,?)";

    Connection c = null;
    boolean closeConn = false;

    try {
      c = getConnection();
      closeConn = (this.conn == null);

      try (PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, b.getItemId());
        ps.setInt(2, b.getUserId());
        ps.setDouble(3, b.getBidValue());
        ps.setTimestamp(4, Timestamp.valueOf(b.getTimestamp()));

        return ps.executeUpdate() > 0;
      }

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    } finally {
      if (closeConn && c != null) {
        try {
          c.close();
        } catch (Exception ignored) {
        }
      }
    }
  }

  public List<BidTransaction> getByItem(int iid) {
    List<BidTransaction> ans = new ArrayList<>();
    String sql = "select * from bid_transactions where itemid = ? order by timestamp asc";

    Connection c = null;
    boolean closeConn = false;

    try {
      c = getConnection();
      closeConn = (this.conn == null);

      try (PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, iid);

        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            BidTransaction b =
                    new BidTransaction(
                            rs.getInt("itemid"),
                            rs.getInt("userid"),
                            rs.getDouble("bidvalue"));
            b.setId(rs.getInt("id"));
            b.setVersion(rs.getInt("version"));
            Timestamp ts = rs.getTimestamp("timestamp");
            if (ts != null) {
              b.setTimestamp(ts.toLocalDateTime());
            }
            ans.add(b);
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (closeConn && c != null) {
        try {
          c.close();
        } catch (Exception ignored) {
        }
      }
    }

    return ans;
  }

  public BidTransaction getWinner(int iid) {
    BidTransaction ans = null;
    String sql =
            "select * from bid_transactions where itemid = ? order by bidvalue desc, timestamp asc limit 1";

    Connection c = null;
    boolean closeConn = false;

    try {
      c = getConnection();
      closeConn = (this.conn == null);

      try (PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, iid);

        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            ans =
                    new BidTransaction(
                            rs.getInt("itemid"),
                            rs.getInt("userid"),
                            rs.getDouble("bidvalue"));
            ans.setId(rs.getInt("id"));
            ans.setVersion(rs.getInt("version"));
            Timestamp ts = rs.getTimestamp("timestamp");
            if (ts != null) {
              ans.setTimestamp(ts.toLocalDateTime());
            }
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (closeConn && c != null) {
        try {
          c.close();
        } catch (Exception ignored) {
        }
      }
    }

    return ans;
  }

  public int getHighestBidderId(int itemId) {
    String sql =
            "select userid from bid_transactions where itemid = ? order by bidvalue desc, timestamp asc limit 1";

    Connection c = null;
    boolean closeConn = false;

    try {
      c = getConnection();
      closeConn = (this.conn == null);

      try (PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, itemId);

        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            return rs.getInt("userid");
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (closeConn && c != null) {
        try {
          c.close();
        } catch (Exception ignored) {
        }
      }
    }

    return -1;
  }
}