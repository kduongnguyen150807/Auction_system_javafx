package com.auction.server.dao;

import com.auction.shared.TransactionLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionLogDao {
    private Connection conn;

    public TransactionLogDao() {
        this.conn = DatabaseConnection.getinstance().getconnection();
    }

    public boolean insertlog(int u, String t, double a, int i) {
        boolean ans = false;
        try {
            String res = "INSERT INTO transaction_logs (userid, type, amount, itemid) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = this.conn.prepareStatement(res);
            ps.setInt(1, u);
            ps.setString(2, t);
            ps.setDouble(3, a);
            ps.setInt(4, i);
            int res2 = ps.executeUpdate();
            ans = res2 > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ans;
    }

    public List<TransactionLog> getbyuserid(int u) {
        List<TransactionLog> ans = new ArrayList<>();
        try {
            String res = "SELECT * FROM transaction_logs WHERE userid = ? ORDER BY createdat DESC";
            PreparedStatement ps = this.conn.prepareStatement(res);
            ps.setInt(1, u);
            ResultSet res2 = ps.executeQuery();
            while (res2.next()) {
                TransactionLog res3 = new TransactionLog();
                res3.setid(res2.getInt("id"));
                res3.setuserid(res2.getInt("userid"));
                res3.settype(res2.getString("type"));
                res3.setamount(res2.getDouble("amount"));
                res3.setitemid(res2.getInt("itemid"));
                res3.setcreatedat(res2.getTimestamp("createdat").toLocalDateTime());
                ans.add(res3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ans;
    }
}