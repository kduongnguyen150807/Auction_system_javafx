package com.auction.server.dao;
import com.auction.shared.*;
import java.sql.*;
public class UserDao {
    private Connection conn;
    public UserDao() {
        this.conn = DatabaseConnection.getinstance().getconnection();
    }
    public User login(String u, String p) {
        User ans = null;
        try {
            String sql = "select * from users where username = ? and password = ? and isactive = true";
            PreparedStatement ps = this.conn.prepareStatement(sql);
            ps.setString(1, u);
            ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String r = rs.getString("role");
                if (r.equalsIgnoreCase("ADMIN")) ans = new Admin();
                else if (r.equalsIgnoreCase("SELLER")) ans = new Seller();
                else ans = new Bidder();
                ans.setid(rs.getInt("id"));
                ans.setversion(rs.getInt("version"));
                ans.setusername(rs.getString("username"));
                ans.setpassword(rs.getString("password"));
                ans.setemail(rs.getString("email"));
                ans.setage(rs.getString("age"));
                ans.setphonenumber(rs.getString("phonenumber"));
                ans.setbalance(rs.getDouble("balance"));
                ans.setactive(rs.getBoolean("isactive"));
                ans.setlocked(rs.getBoolean("islocked"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ans;
    }
    public boolean signup(User u) {
        boolean ans = false;
        try {
            String sql = "insert into users(username, password, email, age, phonenumber, role, isactive, islocked) values(?,?,?,?,?,?,?,?)";
            PreparedStatement ps = this.conn.prepareStatement(sql);
            ps.setString(1, u.getusername());
            ps.setString(2, u.getpassword());
            ps.setString(3, u.getemail());
            ps.setString(4, u.getage());
            ps.setString(5, u.getphonenumber());
            ps.setString(6, u.getrole().name());
            ps.setBoolean(7, true);
            ps.setBoolean(8, false);
            ans = ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return ans;
    }
}