<<<<<<< HEAD
<<<<<<< HEAD
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
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, u);
            ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String r = rs.getString("role");
                if (r.equalsIgnoreCase("admin")) ans = new Admin();
                else if (r.equalsIgnoreCase("seller")) ans = new Seller();
                else ans = new Bidder();
                ans.setid(rs.getInt("id"));
                ans.setusername(rs.getString("username"));
                ans.setemail(rs.getString("email"));
                ans.setactive(rs.getBoolean("isactive"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ans;
    }
}
=======

>>>>>>> ac326420b33c0c98046ce5c55e9215f10777f773
=======

>>>>>>> ac326420b33c0c98046ce5c55e9215f10777f773
