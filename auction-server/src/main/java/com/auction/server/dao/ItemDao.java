package com.auction.server.dao;

import com.auction.shared.Item;
import com.auction.shared.ItemFactory;
import com.auction.shared.ItemStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemDao {
    private static ItemDao instance;
    private Connection conn;
    private ItemDao(){
        this.conn = DatabaseConnection.getInstance().getConnection();
    }
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

    public boolean insertLot(String res,
                             String ans,
                             double res1,
                             double ans1,
                             LocalDateTime res2,
                             LocalDateTime ans2,
                             String res3,
                             String ans3,
                             String res4){
        boolean ans4 = false;
        try{
            int res5 = -1;
            PreparedStatement ps0 = this.conn.prepareStatement("select id from users where username = ? limit 1");
            ps0.setString(1, res3);
            ResultSet rs0 = ps0.executeQuery();
            if(rs0.next()) res5 = rs0.getInt(1);
            if(res5 <= 0) return false;

            String res6 =
                    "INSERT INTO items (category, name, description, startingprice, currentprice, maxprice, starttime, endtime, sellerid, winnerid, status, version, image_url) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement ps = this.conn.prepareStatement(res6);
            ps.setString(1, res4 == null ? "Vehicle" : res4);
            ps.setString(2, res);
            ps.setString(3, ans);
            ps.setDouble(4, res1);
            ps.setDouble(5, res1);
            ps.setDouble(6, ans1);
            ps.setTimestamp(7, Timestamp.valueOf(res2));
            ps.setTimestamp(8, Timestamp.valueOf(ans2));
            ps.setInt(9, res5);
            ps.setNull(10, Types.INTEGER);
            ps.setString(11, ItemStatus.PENDING.name());
            ps.setInt(12, 0);
            ps.setString(13, ans3);
            int res7 = ps.executeUpdate();
            ans4 = res7 > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return ans4;
    }

    public List<Item> getItemByStatus(ItemStatus status){
        List<Item> itemList = new ArrayList<>();
        String sql = "SELECT i.*, u.username AS seller_name, u.avatar_url AS seller_avatar " +
                "FROM items i " +
                "LEFT JOIN users u ON i.sellerid = u.id " +
                "WHERE i.status = ? " +
                "ORDER BY i.id DESC";
        try (PreparedStatement statement = this.conn.prepareStatement(sql)) {
            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    itemList.add(mapResultSet(resultSet));
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching items by status: " + e.getMessage());
        }
        return itemList;
    }

    public boolean approveItem(int itemId){
        boolean isSuccess = false;
        String sql = "UPDATE items SET status = 'OPEN' WHERE id = ? AND status = 'PENDING'";

        try (PreparedStatement statement = this.conn.prepareStatement(sql)) {
            statement.setInt(1, itemId);

            int rowsAffected = statement.executeUpdate();
            isSuccess = rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi duyệt sản phẩm: " + e.getMessage());
        }
        return isSuccess;
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
        }
        return item;
    }
}
