package com.auction.server.dao;

import com.auction.shared.Item.Item;
import com.auction.shared.Item.ItemFactory;
import com.auction.shared.Item.ItemStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemDao extends BaseDao {
    private static ItemDao instance;
    private ItemDao(){}
    public static ItemDao getInstance() {
        if(instance == null){
            synchronized (ItemDao.class){
                if(instance == null) {
                    instance = new ItemDao();
                }
            }
        }
        return instance;
    }

    public boolean insertLot(String itemName, String description, double startingPrice, double maxPrice,
                             LocalDateTime startTime, LocalDateTime endTime, int sellerId,
                             String imageUrl, String category) {

        String sql = "INSERT INTO items (category, name, description, startingprice, currentprice, " +
                "maxprice, starttime, endtime, sellerid, winnerid, status, version, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        List<Object> args = new ArrayList<>();
        args.add(category == null ? "Vehicle" : category);
        args.add(itemName);
        args.add(description);
        args.add(startingPrice);
        args.add(startingPrice);
        args.add(maxPrice);
        args.add(Timestamp.valueOf(startTime));
        args.add(Timestamp.valueOf(endTime));
        args.add(sellerId);
        args.add(null);
        args.add(ItemStatus.PENDING.name());
        args.add(0);
        args.add(imageUrl == null ? "" : imageUrl);

        return executeUpdate(sql, args);
    }

    public int getPreviousHighestBidder(int itemid){
        String sql = "SELECT userid FROM bid_transactions WHERE itemid = ? " +
                "ORDER BY bidvalue DESC LIMIT 1 OFFSET 1";
        List<Integer> results = executeFetch(sql, List.of(itemid), rs -> rs.getInt("userid"));
        return results.isEmpty() ? -1 : results.get(0);
    }

    public boolean updatePrice(int itemId, double boughtPrice, int version){
        String sql = "UPDATE items SET currentprice = ?, version = version + 1 WHERE id = ? AND version = ?";
        return executeUpdate(sql, List.of(boughtPrice, itemId, version));
    }

    public boolean updateEndtime(int itemId, LocalDateTime newEndTime){
        String sql = "UPDATE items SET endtime = ? where id = ?";
        return executeUpdate(sql, List.of(Timestamp.valueOf(newEndTime), itemId));
    }

    public void closeAuction(int itemId, int winnerId, String status){
        String sql = "UPDATE items SET winnerid = ?, status = ? WHERE id =?";
        executeUpdate(sql, List.of(winnerId, status, itemId));
    }

    public List<Item> getItemByStatus(ItemStatus status){
        String sql = "select i.*, u.username as seller_name, u.avatar_url as seller_avatar from items i left join users u on i.sellerid = u.id where i.status = ? order by i.id desc";
        return executeFetch(sql, List.of(String.valueOf(status)), this::mapResultSet);
    }

    public List<Item> getExpiredItems(){
        String sql = "SELECT * FROM items WHERE endtime <= NOW() AND status = 'OPEN' ";
        return executeFetch(sql, null, this::mapResultSet);
    }

    public Item getById(int Id){
        String sql = "SELECT i.*, u.username as seller_name, u.avatar_url as seller_avatar " +
                "FROM items i LEFT JOIN users u ON i.sellerid = u.id " +
                "WHERE i.id = ?";
        List<Item> results = executeFetch(sql, List.of(Id), this::mapResultSet);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean approveItem(int itemId){
        String sql = "UPDATE items SET status = 'OPEN' WHERE id = ? AND status = 'PENDING'";
        return executeUpdate(sql, List.of(itemId));
    }

    private Item mapResultSet(ResultSet resultSet) throws SQLException{
        String category = resultSet.getString("category");
        Item item = ItemFactory.createItem(category);


        item.setId(resultSet.getInt("id"));
        item.setVersion(resultSet.getInt("version"));
        item.setName(resultSet.getString("name"));
        item.setDescription(resultSet.getString("description"));
        item.setStartingPrice(resultSet.getDouble("startingprice"));
        item.setCurrentPrice(resultSet.getDouble("currentprice"));
        item.setMaxPrice(resultSet.getDouble("maxprice"));

        Timestamp startTimeStamp = resultSet.getTimestamp("starttime");
        if (startTimeStamp != null) {
            item.setStartTime(startTimeStamp.toLocalDateTime());
        }

        Timestamp endTimeStamp = resultSet.getTimestamp("endtime");
        if (endTimeStamp != null) {
            item.setEndTime(endTimeStamp.toLocalDateTime());
        }

        item.setSellerId(resultSet.getInt("sellerid"));
        item.setWinnerId(resultSet.getInt("winnerid"));
        item.setStatus(ItemStatus.valueOf(resultSet.getString("status")));
        item.setImageUrl(resultSet.getString("image_url"));
        try {
            String sellerName = resultSet.getString("seller_name");
            if (sellerName != null) {
                item.setSellerUsername(sellerName);
            }
            String sellerAvatar = resultSet.getString("seller_avatar");
            if (sellerAvatar != null) {
                item.setSellerAvatarUrl(sellerAvatar);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error");
        }
        return item;
    }
}
