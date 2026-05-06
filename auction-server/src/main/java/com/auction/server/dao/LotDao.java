package com.auction.server.dao;

import com.auction.shared.Lot;
import com.auction.shared.item.Item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class LotDao extends BaseDao {
  public List<Lot> getOngoingBids() {
    String sql = "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar "
      + "FROM items i "
      + "LEFT JOIN users u ON i.sellerid = u.item_id "
      + "WHERE i.status = 'OPEN' AND i.starttime <= NOW() and i.endtime >= NOW() ";
    return query(sql, null, this::mapResultSet);
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
