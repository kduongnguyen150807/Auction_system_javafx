package com.auction.server.dao;

import com.auction.shared.item.Item;
import com.auction.shared.item.ItemFactory;
import com.auction.shared.item.ItemStatus;
import com.auction.shared.item.ItemType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class ItemDao extends BaseDao{
  public List<Item> getAllItems() {
    String sql = "select * from items";
    return query(sql, null, this::mapResultSet);
  }

  public boolean approveItem(String itemId) {
    String sql = "UPDATE items SET status = 'OPEN' WHERE id = ?";
    return update(sql, List.of(itemId));
  }

  private Item mapResultSet(ResultSet rs) throws SQLException {
    Item item = ItemFactory.createItem(ItemType.valueOf(rs.getString("category").toUpperCase()));
    item.setId(rs.getInt("id")); item.setVersion(rs.getInt("version"));
    item.setName(rs.getString("name")); item.setDescription(rs.getString("description"));
    item.setStartingPrice(rs.getDouble("startingprice")); item.setCurrentPrice(rs.getDouble("currentprice"));
    item.setMaxPrice(rs.getDouble("maxprice"));
    Timestamp startTs = rs.getTimestamp("starttime"); if (startTs != null) item.setStartTime(startTs.toLocalDateTime());
    Timestamp endTs = rs.getTimestamp("endtime"); if (endTs != null) item.setEndTime(endTs.toLocalDateTime());
    item.setSellerId(rs.getInt("sellerid")); item.setWinnerId(rs.getInt("winnerid"));
    item.setStatus(ItemStatus.valueOf(rs.getString("status")));
    item.setImageUrl(rs.getString("image_url"));
    try {
      String sn = rs.getString("seller_name"); if (sn != null) item.setSellerUsername(sn);
      String sa = rs.getString("seller_avatar"); if (sa != null) item.setSellerAvatarUrl(sa);
    } catch (SQLException e) {}
    return item;
  }
}
