<<<<<<< HEAD
<<<<<<< HEAD
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
        boolean res = false;
        try {
            String sql = "insert into bids(itemid,userid,val,ts) values(?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, b.getiid());
            ps.setInt(2, b.getuid());
            ps.setDouble(3, b.getval());
            ps.setTimestamp(4, Timestamp.valueOf(b.getts()));
            res = ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return res;
    }
    public List<BidTransaction> getbyitem(int iid) {
        List<BidTransaction> ans = new ArrayList<>();
        try {
            String sql = "select * from bids where itemid = ? order by ts asc";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, iid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BidTransaction b = new BidTransaction(rs.getInt("itemid"), rs.getInt("userid"), rs.getDouble("val"));
                b.setid(rs.getInt("id"));
                b.setts(rs.getTimestamp("ts").toLocalDateTime());
                ans.add(b);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ans;
    }
    public BidTransaction getwinner(int iid) {
        BidTransaction ans = null;
        try {
            String sql = "select * from bids where itemid = ? order by val desc, ts asc limit 1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, iid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ans = new BidTransaction(rs.getInt("itemid"), rs.getInt("userid"), rs.getDouble("val"));
                ans.setid(rs.getInt("id"));
                ans.setts(rs.getTimestamp("ts").toLocalDateTime());
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ans;
    }
    public List<BidTransaction> getbyuser(int uid) {
        List<BidTransaction> ans = new ArrayList<>();
        try {
            String sql = "select * from bids where userid = ? order by ts desc";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, uid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BidTransaction b = new BidTransaction(rs.getInt("itemid"), rs.getInt("userid"), rs.getDouble("val"));
                b.setid(rs.getInt("id"));
                b.setts(rs.getTimestamp("ts").toLocalDateTime());
                ans.add(b);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ans;
    }
}
=======

>>>>>>> ac326420b33c0c98046ce5c55e9215f10777f773
=======

>>>>>>> ac326420b33c0c98046ce5c55e9215f10777f773
