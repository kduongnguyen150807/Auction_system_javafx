package com.auction.server.dao.auction;

import com.auction.server.dao.platform.BaseDao;
import com.auction.shared.BidTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BidDao extends BaseDao<BidTransaction> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BidDao.class);

  private static final String SELECT_BID_HISTORY_SQL =
          "SELECT * FROM bid_transactions WHERE itemid = ? ORDER BY timestamp ASC";

  private static final String INSERT_BID_SQL =
          "INSERT INTO bid_transactions(itemid, userid, bidvalue, timestamp) VALUES (?, ?, ?, ?)";

  private static final String SELECT_PREVIOUS_HIGHEST_BIDDER_SQL =
          "SELECT userid FROM bid_transactions WHERE itemid = ? ORDER BY bidvalue DESC LIMIT 1";

  private static final String SELECT_OPEN_AUCTION_IDS_FOR_BIDDER_SQL =
          "SELECT DISTINCT b.itemid FROM bid_transactions b "
                  + "JOIN items i ON i.id = b.itemid "
                  + "WHERE b.userid = ? AND i.status = 'OPEN'";

  private static final String DELETE_BIDS_BY_USER_ON_ITEM_SQL =
          "DELETE FROM bid_transactions WHERE itemid = ? AND userid = ?";

  private static final String SELECT_HIGHEST_VALID_BID_SQL =
          "SELECT b.* FROM bid_transactions b "
                  + "JOIN users u ON u.id = b.userid "
                  + "WHERE b.itemid = ? AND u.islocked = false AND u.isactive = true "
                  + "ORDER BY b.bidvalue DESC, b.timestamp ASC LIMIT 1";

  private static final String SELECT_CURRENT_HIGHEST_BIDDER_SQL =
          "SELECT userid FROM bid_transactions WHERE itemid = ? "
                  + "ORDER BY bidvalue DESC, timestamp ASC LIMIT 1";

  @Override
  protected BidTransaction mapRow(ResultSet rs) throws SQLException {
    BidTransaction bid =
            new BidTransaction(
                    rs.getInt("itemid"),
                    rs.getInt("userid"),
                    rs.getDouble("bidvalue"));

    bid.setId(rs.getInt("id"));
    bid.setVersion(rs.getInt("version"));

    Timestamp timestamp = rs.getTimestamp("timestamp");
    if (timestamp != null) {
      bid.setTimestamp(timestamp.toLocalDateTime());
    }

    return bid;
  }

  public List<BidTransaction> getBidHistory(int itemId) {
    List<BidTransaction> history = new ArrayList<>();

    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(SELECT_BID_HISTORY_SQL)) {

      ps.setInt(1, itemId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          history.add(mapRow(rs));
        }
      }
    } catch (Exception e) {
      LOGGER.warn("getBidHistory failed for itemId={}", itemId, e);
    }

    return history;
  }

  public boolean placeBidTx(BidTransaction bid, Connection conn) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement(INSERT_BID_SQL)) {
      ps.setInt(1, bid.getItemId());
      ps.setInt(2, bid.getUserId());
      ps.setDouble(3, bid.getBidValue());
      ps.setTimestamp(4, Timestamp.valueOf(bid.getTimestamp()));

      return ps.executeUpdate() > 0;
    }
  }

  public int getPreviousHighestBidder(int itemId) {
    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(SELECT_PREVIOUS_HIGHEST_BIDDER_SQL)) {

      ps.setInt(1, itemId);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt("userid");
        }
      }
    } catch (Exception e) {
      LOGGER.warn("getPreviousHighestBidder failed for itemId={}", itemId, e);
    }

    return -1;
  }

  public List<Integer> getOpenAuctionIdsForBidder(int userId) {
    List<Integer> ids = new ArrayList<>();

    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(SELECT_OPEN_AUCTION_IDS_FOR_BIDDER_SQL)) {

      ps.setInt(1, userId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          ids.add(rs.getInt(1));
        }
      }
    } catch (Exception e) {
      LOGGER.warn("getOpenAuctionIdsForBidder failed for userId={}", userId, e);
    }

    return ids;
  }

  public int deleteBidsByUserOnItemTx(int itemId, int userId, Connection conn)
          throws SQLException {

    try (PreparedStatement ps = conn.prepareStatement(DELETE_BIDS_BY_USER_ON_ITEM_SQL)) {
      ps.setInt(1, itemId);
      ps.setInt(2, userId);

      return ps.executeUpdate();
    }
  }

  public BidTransaction findHighestValidBidTx(int itemId, Connection conn)
          throws SQLException {

    try (PreparedStatement ps = conn.prepareStatement(SELECT_HIGHEST_VALID_BID_SQL)) {
      ps.setInt(1, itemId);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapRow(rs);
        }
      }
    }

    return null;
  }

  public int getCurrentHighestBidderTx(int itemId, Connection conn)
          throws SQLException {

    try (PreparedStatement ps = conn.prepareStatement(SELECT_CURRENT_HIGHEST_BIDDER_SQL)) {
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