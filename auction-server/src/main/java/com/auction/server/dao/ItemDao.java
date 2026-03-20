<<<<<<< HEAD
<<<<<<< HEAD
package com.auction.server.dao;
import com.auction.shared.*;
import com.auction.server.factory.ItemFactory;
import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;
public class ItemDao {
    private Connection conn;
    public ItemDao() {
        this.conn = DatabaseConnection.getinstance().getconnection();
    }
    public List<Item> getall() {
        List<Item> ans = new ArrayList<>();
        try {
            String sql = "select * from items";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ans.add(maprs(rs));
        } catch (Exception e) {}
        return ans;
    }
    public Item getbyid(int id) {
        Item ans = null;
        try {
            String sql = "select * from items where id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) ans = maprs(rs);
        } catch (Exception e) {}
        return ans;
    }
    public boolean updateprice(int id, double val) {
        boolean res = false;
        try {
            String sql = "update items set currentprice = ? where id = ? and currentprice < ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, val);
            ps.setInt(2, id);
            ps.setDouble(3, val);
            res = ps.executeUpdate() > 0;
        } catch (Exception e) {}
        return res;
    }
    public boolean updateendtime(int id, LocalDateTime time) {
        boolean res = false;
        try {
            String sql = "update items set endtime = ? where id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, Timestamp.valueOf(time));
            ps.setInt(2, id);
            res = ps.executeUpdate() > 0;
        } catch (Exception e) {}
        return res;
    }
    private Item maprs(ResultSet rs) throws SQLException {
        Item i = ItemFactory.createitem(rs.getString("type"));
        i.setid(rs.getInt("id"));
        i.setname(rs.getString("name"));
        i.setdescription(rs.getString("description"));
        i.setstartingprice(rs.getDouble("startingprice"));
        i.setcurrentprice(rs.getDouble("currentprice"));
        i.setstarttime(rs.getTimestamp("starttime").toLocalDateTime());
        i.setendtime(rs.getTimestamp("endtime").toLocalDateTime());
        return i;
    }
}
=======

>>>>>>> ac326420b33c0c98046ce5c55e9215f10777f773
=======

>>>>>>> ac326420b33c0c98046ce5c55e9215f10777f773
