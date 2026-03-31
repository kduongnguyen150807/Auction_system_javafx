package com.auction.server.dao;

import com.auction.shared.*;
import java.sql.*;

public class UserDao {
  private Connection conn;

  public UserDao() {
    this.conn = DatabaseConnection.getinstance().getconnection();
    this.ensureUniqueIndexes();
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
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public boolean signup(User u) {
    boolean ans = false;
    try {
      String normalizedUsername = normalize(u.getusername());
      String normalizedEmail = normalize(u.getemail());
      if (this.existsDuplicateUser(normalizedUsername, normalizedEmail)) {
        return false;
      }

      String sql =
          "insert into users(username, password, email, age, phonenumber, role, isactive, islocked) values(?,?,?,?,?,?,?,?)";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setString(1, normalizedUsername);
      ps.setString(2, u.getpassword());
      ps.setString(3, normalizedEmail);
      ps.setString(4, u.getage());
      ps.setString(5, u.getphonenumber());
      ps.setString(6, u.getrole().name());
      ps.setBoolean(7, true);
      ps.setBoolean(8, false);
      ans = ps.executeUpdate() > 0;
    } catch (SQLIntegrityConstraintViolationException e) {
      // Duplicate username/email unique index violation.
      ans = false;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  private void ensureUniqueIndexes() {
    try {
      if (!this.indexExists("users", "uk_users_username")) {
        Statement st = this.conn.createStatement();
        st.execute("create unique index uk_users_username on users(username)");
      }
      if (!this.indexExists("users", "uk_users_email")) {
        Statement st = this.conn.createStatement();
        st.execute("create unique index uk_users_email on users(email)");
      }
    } catch (SQLException e) {
      // Keep server alive even if index creation fails.
      e.printStackTrace();
    }
  }

  private boolean indexExists(String tableName, String indexName) throws SQLException {
    String sql =
        "select 1 from information_schema.statistics "
            + "where table_schema = database() and table_name = ? and index_name = ? limit 1";
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.setString(1, tableName);
    ps.setString(2, indexName);
    ResultSet rs = ps.executeQuery();
    return rs.next();
  }

  private boolean existsDuplicateUser(String username, String email) throws SQLException {
    String sql =
        "select 1 from users where lower(trim(username)) = lower(trim(?)) or lower(trim(email)) = lower(trim(?)) limit 1";
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.setString(1, username);
    ps.setString(2, email);
    ResultSet rs = ps.executeQuery();
    return rs.next();
  }

  private String normalize(String value) {
    if (value == null) return "";
    return value.trim();
  }
}
