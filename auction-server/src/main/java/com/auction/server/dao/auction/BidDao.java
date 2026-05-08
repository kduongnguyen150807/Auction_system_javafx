package com.auction.server.dao.auction;

import com.auction.server.dao.platform.BaseDao;

import com.auction.shared.BidTransaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BidDao extends BaseDao<BidTransaction> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BidDao.class);

  @Override
  protected BidTransaction mapRow(ResultSet rs) throws SQLException {
    BidTransaction bid = new BidTransaction(rs.getInt("itemid"), rs.getInt("userid"), rs.getDouble("bidvalue"));
    bid.setId(rs.getInt("id"));
    bid.setVersion(rs.getInt("version"));
    bid.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
    return bid;
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
      LOGGER.warn("placeBid failed", e);
      return false;
    }
  }

  public List<BidTransaction> getByItem(int itemId) {
    return queryList("select * from bid_transactions where itemid = ? order by timestamp asc", itemId);
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
      LOGGER.warn("getBidHistory failed", e);
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
      LOGGER.warn("getPreviousHighestBidder failed", e);
    }
    return -1;
  }

  // ─── Ban-handling helpers (transactional, share a Connection) ─────────────

  /** Returns item ids of OPEN auctions where this user has placed at least one bid. */
  public List<Integer> getOpenAuctionIdsForBidder(int userId) {
    List<Integer> ids = new ArrayList<>();
    String sql =
        "SELECT DISTINCT b.itemid FROM bid_transactions b "
            + "JOIN items i ON i.id = b.itemid "
            + "WHERE b.userid = ? AND i.status = 'OPEN'";
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, userId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        ids.add(rs.getInt(1));
      }
    } catch (Exception e) {
      LOGGER.warn("getOpenAuctionIdsForBidder failed", e);
    }
    return ids;
  }

  /** Deletes (invalidates) every bid placed by the banned user on the auction. */
  public int deleteBidsByUserOnItemTx(int itemId, int userId, Connection conn) throws SQLException {
    String sql = "DELETE FROM bid_transactions WHERE itemid = ? AND userid = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemId);
      ps.setInt(2, userId);
      return ps.executeUpdate();
    }
  }

  /**
   * Highest still-valid bid (i.e. the new auction leader) on the supplied connection.
   * Bids by locked / inactive users are skipped so banned bidders never become leader.
   */
  public BidTransaction findHighestValidBidTx(int itemId, Connection conn) throws SQLException {
    String sql =
        "SELECT b.* FROM bid_transactions b "
            + "JOIN users u ON u.id = b.userid "
            + "WHERE b.itemid = ? AND u.islocked = false AND u.isactive = true "
            + "ORDER BY b.bidvalue DESC, b.timestamp ASC LIMIT 1";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapRow(rs);
        }
      }
    }
    return null;
  }

  /** Current top bidder id on the supplied connection (-1 when no bids exist). */
  public int getCurrentHighestBidderTx(int itemId, Connection conn) throws SQLException {
    String sql =
        "SELECT userid FROM bid_transactions WHERE itemid = ? "
            + "ORDER BY bidvalue DESC, timestamp ASC LIMIT 1";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, itemId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt("userid");
        }
      }
    }
    return -1;
  }
}
