package com.auction.server.dao;

import com.auction.shared.Lot;
import com.auction.shared.item.Item;
import com.auction.shared.item.ItemStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class LotDao extends BaseDao {
  public List<Lot> getOngoingBids() {
    String sql = "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar "
      + "FROM items i "
      + "LEFT JOIN users u ON i.sellerid = u.id "
      + "WHERE i.status = 'OPEN' AND i.starttime <= NOW() and i.endtime >= NOW() ";
    return query(sql, null, this::mapResultSet);
  }

  public boolean registerLot(Item item, int sellerId) {
    String sql =
      "INSERT INTO items (category, name, description, startingprice, currentprice, maxprice, starttime, endtime, sellerid, winnerid, status, version, image_url) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

    List<Object> params = new ArrayList<>();
    params.add(item.getCategory().name());
    params.add(item.getName());
    params.add(item.getDescription());
    params.add(item.getStartingPrice());
    params.add(item.getStartingPrice());
    params.add(item.getMaxPrice());
    params.add(Timestamp.valueOf(item.getStartTime()));
    params.add(Timestamp.valueOf(item.getEndTime()));
    params.add(sellerId);
    params.add(null);
    params.add(ItemStatus.PENDING.name());
    params.add(0);
    params.add(item.getImageUrl());

    return update(sql, params);
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
