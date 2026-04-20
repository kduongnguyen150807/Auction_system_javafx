package com.auction.server.dao;

import com.auction.shared.BidTransaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BidDao {
  private static final Logger LOGGER = Logger.getLogger(BidDao.class.getName());

  private Connection getConn() {
    return DatabaseConnection.getInstance().getConnection();
  }

  public boolean placeBid(BidTransaction bid) {
    try (Connection conn = getConn()) {
      conn.setAutoCommit(false);
      try {
        String insertSql = "insert into bid_transactions(itemid,userid,bidvalue,timestamp) values(?,?,?,?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
          insertStmt.setInt(1, bid.getItemId());
          insertStmt.setInt(2, bid.getUserId());
          insertStmt.setDouble(3, bid.getBidValue());
          insertStmt.setTimestamp(4, Timestamp.valueOf(bid.getTimestamp()));
          int inserted = insertStmt.executeUpdate();
          if (inserted > 0) {
            String updateSql = "update items set currentprice = ? where id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
              updateStmt.setDouble(1, bid.getBidValue());
              updateStmt.setInt(2, bid.getItemId());
              updateStmt.executeUpdate();
            }
          }
          conn.commit();
          return inserted > 0;
        }
      } catch (Exception e) {
        conn.rollback();
        throw e;
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "placeBid failed", e);
      return false;
    }
  }

  public boolean addBid(BidTransaction bid) {
    String sql = "insert into bid_transactions(itemid,userid,bidvalue,timestamp) values(?,?,?,?)";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, bid.getItemId());
      ps.setInt(2, bid.getUserId());
      ps.setDouble(3, bid.getBidValue());
      ps.setTimestamp(4, Timestamp.valueOf(bid.getTimestamp()));
      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "addBid failed", e);
      return false;
    }
  }

  public List<BidTransaction> getByItem(int itemId) {
    List<BidTransaction> bids = new ArrayList<>();
    String sql = "select * from bid_transactions where itemid = ? order by timestamp asc";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) bids.add(mapResultSet(rs));
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "getByItem failed", e);
    }
    return bids;
  }

  public BidTransaction getWinner(int itemId) {
    String sql = "select * from bid_transactions where itemid = ? order by bidvalue desc, timestamp asc limit 1";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) return mapResultSet(rs);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "getWinner failed", e);
    }
    return null;
  }

  public List<BidTransaction> getBidHistory(int itemId) {
    List<BidTransaction> history = new ArrayList<>();
    String sql = "SELECT * FROM bid_transactions WHERE itemid = ? ORDER BY timestamp ASC";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        BidTransaction bid = new BidTransaction();
        bid.setBidValue(rs.getDouble("bidvalue"));
        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) bid.setTimestamp(ts.toLocalDateTime());
        history.add(bid);
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "getBidHistory failed", e);
    }
    return history;
  }

  public int getPreviousHighestBidder(int itemId) {
    String sql = "SELECT userid FROM bid_transactions WHERE itemid = ? ORDER BY bidvalue DESC LIMIT 1";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) return rs.getInt("userid");
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "getPreviousHighestBidder failed", e);
    }
    return -1;
  }

  private BidTransaction mapResultSet(ResultSet rs) throws SQLException {
    BidTransaction bid = new BidTransaction(rs.getInt("itemid"), rs.getInt("userid"), rs.getDouble("bidvalue"));
    bid.setId(rs.getInt("id"));
    bid.setVersion(rs.getInt("version"));
    bid.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
    return bid;
  }
}
