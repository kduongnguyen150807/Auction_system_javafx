package com.auction.server.dao;
import com.auction.shared.*;
import java.sql.*;
import java.util.*;
public class LotDao {
    private Connection conn;
    public LotDao() {
        this.conn = DatabaseConnection.getinstance().getconnection();
    }
    public List<Lot> getongoingbids(int userid) {
        List<Lot> ans = new ArrayList<>();
        try {
            String sql = "select distinct i.id, i.name, i.description, i.startingprice, i.currentprice, i.starttime, i.endtime, i.image_url from items i inner join bid_transactions b on b.itemid = i.id where b.userid = ? and i.starttime <= now() and i.endtime >= now() order by i.endtime asc";
            PreparedStatement ps = this.conn.prepareStatement(sql);
            ps.setInt(1, userid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Lot res = new Lot();
                res.setid(rs.getInt("id"));
                res.settitle(rs.getString("name"));
                res.setdescription(rs.getString("description"));
                res.setstartprice(rs.getDouble("startingprice"));
                res.setbidvalue(rs.getDouble("currentprice"));
                res.setstarttime(rs.getTimestamp("starttime").toLocalDateTime());
                res.setendtime(rs.getTimestamp("endtime").toLocalDateTime());
                String res2 = rs.getString("image_url");
                if (res2 != null && !res2.isBlank()) {
                    res.setimageurl(res2);
                }
                ans.add(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ans;
    }
    public List<Lot> getupcomingbids(int userid) {
        List<Lot> ans = new ArrayList<>();
        try {
            String sql = "select distinct i.id, i.name, i.description, i.startingprice, i.currentprice, i.starttime, i.endtime, i.image_url from items i inner join bid_transactions b on b.itemid = i.id where b.userid = ? and i.starttime > now() order by i.starttime asc";
            PreparedStatement ps = this.conn.prepareStatement(sql);
            ps.setInt(1, userid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Lot res = new Lot();
                res.setid(rs.getInt("id"));
                res.settitle(rs.getString("name"));
                res.setdescription(rs.getString("description"));
                res.setstartprice(rs.getDouble("startingprice"));
                res.setbidvalue(rs.getDouble("currentprice"));
                res.setstarttime(rs.getTimestamp("starttime").toLocalDateTime());
                res.setendtime(rs.getTimestamp("endtime").toLocalDateTime());
                String res2 = rs.getString("image_url");
                if (res2 != null && !res2.isBlank()) {
                    res.setimageurl(res2);
                }
                ans.add(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ans;
    }
}
