package com.auction.server.dao;
import com.auction.shared.*;
import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;
public class BidDao {
    private Connection conn;
    public BidDao() {
        this.conn = DatabaseConnection.getinstance().getconnection();
    }
    public boolean addbid(BidTransaction b) {
        boolean ans = false;
        try {
            String sql = "insert into bid_transactions(itemid,userid,bidvalue,timestamp) values(?,?,?,?)";
            PreparedStatement ps = this.conn.prepareStatement(sql);
            ps.setInt(1, b.getitemid());
            ps.setInt(2, b.getuserid());
            ps.setDouble(3, b.getbidvalue());
            ps.setTimestamp(4, Timestamp.valueOf(b.gettimestamp()));
            ans = ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return ans;
    }
    public List<BidTransaction> getbyitem(int iid) {
        List<BidTransaction> ans = new ArrayList<>();
        try {
            String sql = "select * from bid_transactions where itemid = ? order by timestamp asc";
            PreparedStatement ps = this.conn.prepareStatement(sql);
            ps.setInt(1, iid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BidTransaction b = new BidTransaction(rs.getInt("itemid"), rs.getInt("userid"), rs.getDouble("bidvalue"));
                b.setid(rs.getInt("id"));
                b.setversion(rs.getInt("version"));
                b.settimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                ans.add(b);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ans;
    }
    public BidTransaction getwinner(int iid) {
        BidTransaction ans = null;
        try {
            String sql = "select * from bid_transactions where itemid = ? order by bidvalue desc, timestamp asc limit 1";
            PreparedStatement ps = this.conn.prepareStatement(sql);
            ps.setInt(1, iid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ans = new BidTransaction(rs.getInt("itemid"), rs.getInt("userid"), rs.getDouble("bidvalue"));
                ans.setid(rs.getInt("id"));
                ans.setversion(rs.getInt("version"));
                ans.settimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ans;
    }
}