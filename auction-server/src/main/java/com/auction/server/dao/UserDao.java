package com.auction.server.dao;

import com.auction.shared.*;
import java.sql.*;

public class UserDao {
  private Connection conn;

  public UserDao() {
    this.conn = DatabaseConnection.getinstance().getconnection();
    this.ensureProfileColumns();
    this.ensureUniqueIndexes();
  }

  private void ensureProfileColumns() {
    try {
      if (!this.columnExists("users", "fullname")) {
        Statement st = this.conn.createStatement();
        st.execute("alter table users add column fullname varchar(255) null");
        st.execute("update users set fullname = username where fullname is null or trim(fullname) = ''");
      }
      if (!this.columnExists("users", "avgrating")) {
        Statement st = this.conn.createStatement();
        st.execute("alter table users add column avgrating double default 0");
      }
      if (!this.columnExists("users", "totalratings")) {
        Statement st = this.conn.createStatement();
        st.execute("alter table users add column totalratings int default 0");
      }
    } catch (SQLException e) {
    }
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
        ans.setfullname(rs.getString("fullname"));
        ans.setpassword(rs.getString("password"));
        ans.setemail(rs.getString("email"));
        ans.setage(rs.getString("age"));
        ans.setphonenumber(rs.getString("phonenumber"));
        ans.setbalance(rs.getDouble("balance"));
        ans.setmoneyspent(rs.getDouble("moneyspent"));
        ans.setitemsbought(rs.getInt("itemsbought"));
        ans.setmoneyreceived(rs.getDouble("moneyreceived"));
        ans.setitemssold(rs.getInt("itemssold"));
        ans.setactive(rs.getBoolean("isactive"));
        ans.setlocked(rs.getBoolean("islocked"));
        ans.setavatarurl(rs.getString("avatar_url"));
        try { ans.setavgrating(rs.getDouble("avgrating")); } catch (Exception e) {}
        try { ans.settotalratings(rs.getInt("totalratings")); } catch (Exception e) {}
      }
    } catch (Exception e) {
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
              "insert into users(username, fullname, password, email, age, phonenumber, role, isactive, islocked) values(?,?,?,?,?,?,?,?,?)";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setString(1, normalizedUsername);
      String profileName = normalize(u.getfullname());
      if (profileName.isBlank()) profileName = normalizedUsername;
      ps.setString(2, profileName);
      ps.setString(3, u.getpassword());
      ps.setString(4, normalizedEmail);
      ps.setString(5, u.getage());
      ps.setString(6, u.getphonenumber());
      ps.setString(7, u.getrole().name());
      ps.setBoolean(8, true);
      ps.setBoolean(9, false);
      ans = ps.executeUpdate() > 0;
    } catch (SQLIntegrityConstraintViolationException e) {
      ans = false;
    } catch (Exception e) {
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

  private boolean columnExists(String tableName, String columnName) throws SQLException {
    String sql =
            "select 1 from information_schema.columns "
                    + "where table_schema = database() and table_name = ? and column_name = ? limit 1";
    PreparedStatement ps = this.conn.prepareStatement(sql);
    ps.setString(1, tableName);
    ps.setString(2, columnName);
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

  public String updateuserprofile(int userid, String fullname, String email, String phone) {
    String fn = normalize(fullname);
    String em = normalize(email);
    String ph = normalize(phone);
    if (em.isEmpty()) {
      return "invalid_email";
    }
    try {
      if (emailTakenByOtherUser(userid, em)) {
        return "duplicate_email";
      }
      if (!ph.isEmpty() && phoneTakenByOtherUser(userid, ph)) {
        return "duplicate_phone";
      }
      String sql = "UPDATE users SET fullname = ?, email = ?, phonenumber = ? WHERE id = ?";
      try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
        stmt.setString(1, fn);
        stmt.setString(2, em);
        stmt.setString(3, ph);
        stmt.setInt(4, userid);
        stmt.executeUpdate();
      }
      return null;
    } catch (SQLIntegrityConstraintViolationException e) {
      return "duplicate_email";
    } catch (SQLException e) {
      return "update_failed";
    }
  }

  private boolean emailTakenByOtherUser(int userid, String email) throws SQLException {
    String sql =
            "select 1 from users where id <> ? and lower(trim(email)) = lower(trim(?)) limit 1";
    try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
      ps.setInt(1, userid);
      ps.setString(2, email);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  private boolean phoneTakenByOtherUser(int userid, String phone) throws SQLException {
    String sql =
            "select 1 from users where id <> ? and trim(coalesce(phonenumber,'')) <> '' "
                    + "and lower(trim(phonenumber)) = lower(trim(?)) limit 1";
    try (PreparedStatement ps = this.conn.prepareStatement(sql)) {
      ps.setInt(1, userid);
      ps.setString(2, phone);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  public void updateavatar(String username, String ans) throws Exception {
    String query = "UPDATE users SET avatar_url = ? WHERE username = ?";
    java.sql.PreparedStatement stmt = this.conn.prepareStatement(query);
    stmt.setString(1, ans);
    stmt.setString(2, username);
    int res = stmt.executeUpdate();
  }

  public boolean setuserlocked(String username, boolean lockstatus) {
    boolean ans = false;
    String sql = "UPDATE users SET islocked = ? WHERE username = ?";
    try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
      stmt.setBoolean(1, lockstatus);
      stmt.setString(2, username);
      int res = stmt.executeUpdate();
      ans = res > 0;
    } catch (SQLException e) {
    }
    return ans;
  }

  public boolean setuserrole(String username, String role) {
    boolean ans = false;
    String sql = "UPDATE users SET role = ? WHERE username = ?";
    try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
      stmt.setString(1, role);
      stmt.setString(2, username);
      int res = stmt.executeUpdate();
      ans = res > 0;
    } catch (SQLException e) {
    }
    return ans;
  }

  public java.util.List<User> getallusers() {
    java.util.List<User> ans = new java.util.ArrayList<>();
    String sql = "select * from users";
    try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        String r = rs.getString("role");
        User u;
        if (r.equalsIgnoreCase("ADMIN")) u = new Admin();
        else if (r.equalsIgnoreCase("SELLER")) u = new Seller();
        else u = new Bidder();
        u.setid(rs.getInt("id"));
        u.setversion(rs.getInt("version"));
        u.setusername(rs.getString("username"));
        u.setfullname(rs.getString("fullname"));
        u.setpassword(rs.getString("password"));
        u.setemail(rs.getString("email"));
        u.setage(rs.getString("age"));
        u.setphonenumber(rs.getString("phonenumber"));
        u.setbalance(rs.getDouble("balance"));
        u.setmoneyspent(rs.getDouble("moneyspent"));
        u.setitemsbought(rs.getInt("itemsbought"));
        u.setmoneyreceived(rs.getDouble("moneyreceived"));
        u.setitemssold(rs.getInt("itemssold"));
        u.setactive(rs.getBoolean("isactive"));
        u.setlocked(rs.getBoolean("islocked"));
        u.setavatarurl(rs.getString("avatar_url"));
        try { u.setavgrating(rs.getDouble("avgrating")); } catch (Exception e) {}
        try { u.settotalratings(rs.getInt("totalratings")); } catch (Exception e) {}
        ans.add(u);
      }
    } catch (SQLException e) {
    }
    return ans;
  }

  public boolean updatebalance(int id, double b) {
    boolean ans = false;
    try {
      String sql = "UPDATE users SET balance = ? WHERE id = ?";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setDouble(1, b);
      ps.setInt(2, id);
      int res = ps.executeUpdate();
      ans = res > 0;
    } catch (Exception e) {}
    return ans;
  }

  public boolean addbiddermetrics(int userid, double amount) {
    boolean ans = false;
    try {
      String sql = "UPDATE users SET moneyspent = moneyspent + ?, itemsbought = itemsbought + 1 WHERE id = ?";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setDouble(1, amount);
      ps.setInt(2, userid);
      int res = ps.executeUpdate();
      ans = res > 0;
    } catch (Exception e) {}
    return ans;
  }

  public boolean addsellermetrics(int userid, double amount) {
    boolean ans = false;
    try {
      String sql = "UPDATE users SET moneyreceived = moneyreceived + ?, itemssold = itemssold + 1 WHERE id = ?";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setDouble(1, amount);
      ps.setInt(2, userid);
      int res = ps.executeUpdate();
      ans = res > 0;
    } catch (Exception e) {}
    return ans;
  }

  public User getbyid(String id) {
    User ans = null;
    try {
      String sql = "SELECT * FROM users WHERE id = ?";
      PreparedStatement ps = this.conn.prepareStatement(sql);
      ps.setInt(1, Integer.parseInt(id));
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        String r = rs.getString("role");
        if (r.equalsIgnoreCase("ADMIN")) ans = new Admin();
        else if (r.equalsIgnoreCase("SELLER")) ans = new Seller();
        else ans = new Bidder();
        ans.setid(rs.getInt("id"));
        ans.setversion(rs.getInt("version"));
        ans.setusername(rs.getString("username"));
        ans.setfullname(rs.getString("fullname"));
        ans.setpassword(rs.getString("password"));
        ans.setemail(rs.getString("email"));
        ans.setage(rs.getString("age"));
        ans.setphonenumber(rs.getString("phonenumber"));
        ans.setbalance(rs.getDouble("balance"));
        ans.setmoneyspent(rs.getDouble("moneyspent"));
        ans.setitemsbought(rs.getInt("itemsbought"));
        ans.setmoneyreceived(rs.getDouble("moneyreceived"));
        ans.setitemssold(rs.getInt("itemssold"));
        ans.setactive(rs.getBoolean("isactive"));
        ans.setlocked(rs.getBoolean("islocked"));
        ans.setavatarurl(rs.getString("avatar_url"));
        try { ans.setavgrating(rs.getDouble("avgrating")); } catch (Exception e) {}
        try { ans.settotalratings(rs.getInt("totalratings")); } catch (Exception e) {}
      }
    } catch (Exception e) {}
    return ans;
  }
}