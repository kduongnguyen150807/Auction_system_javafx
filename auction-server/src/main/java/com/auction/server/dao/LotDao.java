package com.auction.server.dao;

import com.auction.shared.Lot;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LotDao {
    private Connection conn;

    public LotDao() {
        this.conn = DatabaseConnection.getinstance().getconnection();
    }

    public List<Lot> getongoingbids(int userid) {
        List<Lot> ans = new ArrayList<>();
        try {
            String sql = "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar " +
                    "FROM items i " +
                    "LEFT JOIN users u ON i.sellerid = u.id " +
                    "WHERE i.status = 'OPEN' AND i.starttime <= NOW() AND i.endtime > NOW()";
            PreparedStatement ps = this.conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ans.add(maprs(rs));
            }
        } catch (Exception e) {}
        return ans;
    }

    public List<Lot> getupcomingbids(int userid) {
        List<Lot> ans = new ArrayList<>();
        try {
            String sql = "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar " +
                    "FROM items i " +
                    "LEFT JOIN users u ON i.sellerid = u.id " +
                    "WHERE i.status = 'OPEN' AND i.starttime > NOW()";
            PreparedStatement ps = this.conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ans.add(maprs(rs));
            }
        } catch (Exception e) {}
        return ans;
    }

    public List<Lot> getclosedbids(int userid) {
        List<Lot> ans = new ArrayList<>();
        try {
            String sql = "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar, w.username as w_name " +
                    "FROM items i " +
                    "LEFT JOIN users u ON i.sellerid = u.id " +
                    "LEFT JOIN users w ON i.winnerid = w.id " +
                    "WHERE i.status = 'CLOSED'";
            PreparedStatement ps = this.conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ans.add(maprs(rs));
            }
        } catch (Exception e) {}
        return ans;
    }

    public List<Lot> getpastbids(int userid) {
        List<Lot> ans = new ArrayList<>();
        try {
            String sql = "SELECT i.*, u.username as s_name, u.avatar_url as s_avatar, w.username as w_name " +
                    "FROM items i " +
                    "LEFT JOIN users u ON i.sellerid = u.id " +
                    "LEFT JOIN users w ON i.winnerid = w.id " +
                    "WHERE i.status IN ('FINISHED', 'CANCELED') OR (i.status = 'OPEN' AND i.endtime <= NOW())";
            PreparedStatement ps = this.conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ans.add(maprs(rs));
            }
        } catch (Exception e) {}
        return ans;
    }

    private Lot maprs(ResultSet rs) throws SQLException {
        Lot ans = new Lot();
        ans.setid(rs.getInt("id"));
        ans.settitle(rs.getString("name"));
        ans.setdescription(rs.getString("description"));
        ans.setbidvalue(rs.getDouble("currentprice"));
        ans.setstarttime(rs.getTimestamp("starttime").toLocalDateTime());
        ans.setendtime(rs.getTimestamp("endtime").toLocalDateTime());
        ans.setimageurl(rs.getString("image_url"));
        ans.setsellerusername(rs.getString("s_name"));
        ans.setselleravatarurl(rs.getString("s_avatar"));
        try {
            String wname = rs.getString("w_name");
            if (wname != null) ans.setwinnerusername(wname);
        } catch (Exception e) {}
        return ans;
    }
}