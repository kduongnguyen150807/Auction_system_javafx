package com.auction.server.dao;

import com.auction.shared.*;

import java.sql.*;

public class UserDao {

  public UserDao() {
    this.ensureProfileColumns();
    this.ensureUniqueIndexes();
  }

  private void ensureProfileColumns() {
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
          Statement st = conn.createStatement()){
          if (!this.columnExists(conn,"users", "fullname")) {
            st.execute("alter table users add column fullname varchar(255) null");
            st.execute("update users set fullname = username where fullname is null or trim(fullname) = ''");
          }
          if (!this.columnExists(conn,"users", "avgrating")) {
            st.execute("alter table users add column avgrating double default 0");
          }
          if (!this.columnExists(conn,"users", "totalratings")) {
            st.execute("alter table users add column totalratings int default 0");
          }
    } catch (SQLException e) {
    }
  }

  public User login(String u, String p) {
    User ans = null;
    String sql = "select * from users where username = ? and password = ? and isactive = true";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
        ps.setString(1, u);
        ps.setString(2, p);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          String r = rs.getString("role");
          if (r.equalsIgnoreCase("ADMIN")) ans = new Admin();
          else if (r.equalsIgnoreCase("SELLER")) ans = new Seller();
          else ans = new Bidder();
          ans.setId(rs.getInt("id"));
          ans.setVersion(rs.getInt("version"));
          ans.setUsername(rs.getString("username"));
          ans.setFullName(rs.getString("fullname"));
          ans.setPassword(rs.getString("password"));
          ans.setEmail(rs.getString("email"));
          ans.setAge(rs.getString("age"));
          ans.setPhoneNumber(rs.getString("phonenumber"));
          ans.setBalance(rs.getDouble("balance"));
          ans.setMoneySpent(rs.getDouble("moneyspent"));
          ans.setItemsBought(rs.getInt("itemsbought"));
          ans.setMoneyReceived(rs.getDouble("moneyreceived"));
          ans.setItemsSold(rs.getInt("itemssold"));
          ans.setActive(rs.getBoolean("isactive"));
          ans.setLocked(rs.getBoolean("islocked"));
          ans.setAvatarUrl(rs.getString("avatar_url"));
          try {
            ans.setAvgRating(rs.getDouble("avgrating"));
          } catch (Exception e) {
          }
          try {
            ans.setTotalRatings(rs.getInt("totalratings"));
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  public boolean signup(User u) {
    boolean ans = false;
    String normalizedUsername = normalize(u.getUsername());
    String normalizedEmail = normalize(u.getEmail());
    try (Connection conn = DatabaseConnection.getInstance().getConnection()){
      if (this.existsDuplicateUser(normalizedUsername, normalizedEmail)) {
        return false;
      }

      String sql =
          "insert into users(username, fullname, password, email, age, phonenumber, role, isactive, islocked) values(?,?,?,?,?,?,?,?,?)";
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, normalizedUsername);
        String profileName = normalize(u.getFullName());
        if (profileName.isBlank()) profileName = normalizedUsername;
        ps.setString(2, profileName);
        ps.setString(3, u.getPassword());
        ps.setString(4, normalizedEmail);
        ps.setString(5, u.getAge());
        ps.setString(6, u.getPhoneNumber());
        ps.setString(7, u.getRole().name());
        ps.setBoolean(8, true);
        ps.setBoolean(9, false);
        ans = ps.executeUpdate() > 0;
      }
    } catch (SQLIntegrityConstraintViolationException e) {
      ans = false;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ans;
  }

  private void ensureUniqueIndexes() {
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
          Statement st = conn.createStatement()){
      if (!this.indexExists(conn,"users", "uk_users_username")) {
        st.execute("create unique index uk_users_username on users(username)");
      }
      if (!this.indexExists(conn,"users", "uk_users_email")) {
        st.execute("create unique index uk_users_email on users(email)");
      }
    } catch (SQLException e) {
    }
    catch (Exception e){
      e.printStackTrace();
    }
  }

  private boolean indexExists(Connection conn,String tableName, String indexName) throws SQLException {
    String sql =
        "select 1 from information_schema.statistics "
            + "where table_schema = database() and table_name = ? and index_name = ? limit 1";
    try (PreparedStatement ps = conn.prepareStatement(sql)){
      ps.setString(1, tableName);
      ps.setString(2, indexName);
      try (ResultSet rs = ps.executeQuery()){
        return rs.next();
      }
    }
  }

  private boolean columnExists(Connection conn,String tableName, String columnName) throws SQLException {
    String sql =
        "select 1 from information_schema.columns "
            + "where table_schema = database() and table_name = ? and column_name = ? limit 1";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tableName);
      ps.setString(2 , columnName);
      try (ResultSet rs = ps.executeQuery()){
        return rs.next();
      }
    }
  }

  private boolean existsDuplicateUser(String username, String email) throws SQLException {
    String sql =
        "select 1 from users where lower(trim(username)) = lower(trim(?)) or lower(trim(email)) = lower(trim(?)) limit 1";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
          PreparedStatement ps = conn.prepareStatement(sql)) {
          ps.setString(1, username);
          ps.setString(2, email);
          try (ResultSet rs = ps.executeQuery()) {
            return rs.next();
          }
    }
  }

  private String normalize(String value) {
    if (value == null) return "";
    return value.trim();
  }

  public String updateUserProfile(int userId, String fullName, String email, String phone) {
    String fn = normalize(fullName);
    String em = normalize(email);
    String ph = normalize(phone);
    if (em.isEmpty()) {
      return "invalid_email";
    }
    try (Connection conn = DatabaseConnection.getInstance().getConnection()){
      if (emailTakenByOtherUser(userId, em)) {
        return "duplicate_email";
      }
      if (!ph.isEmpty() && phoneTakenByOtherUser(userId, ph)) {
        return "duplicate_phone";
      }
      String sql = "UPDATE users SET fullname = ?, email = ?, phonenumber = ? WHERE id = ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, fn);
        stmt.setString(2, em);
        stmt.setString(3, ph);
        stmt.setInt(4, userId);
        stmt.executeUpdate();
      }
      return null;
    } catch (SQLIntegrityConstraintViolationException e) {
      return "duplicate_email";
    } catch (SQLException e) {
      return "update_failed";
    }
  }

  private boolean emailTakenByOtherUser(int userId, String email) throws SQLException {
    String sql =
        "select 1 from users where id <> ? and lower(trim(email)) = lower(trim(?)) limit 1";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, userId);
        ps.setString(2, email);
        try (ResultSet rs = ps.executeQuery()) {
          return rs.next();
        }
    }
  }

  private boolean phoneTakenByOtherUser(int userId, String phone) throws SQLException {
    String sql =
        "select 1 from users where id <> ? and trim(coalesce(phonenumber,'')) <> '' "
            + "and lower(trim(phonenumber)) = lower(trim(?)) limit 1";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, userId);
        ps.setString(2, phone);
        try (ResultSet rs = ps.executeQuery()) {
          return rs.next();
        }
    }
  }

  public void updateAvatar(String username, String ans) throws Exception {
    String sql = "UPDATE users SET avatar_url = ? WHERE username = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, ans);
        stmt.setString(2, username);
        int res = stmt.executeUpdate();
    }
  }

  public boolean setUserLocked(String username, boolean lockStatus) {
    boolean ans = false;
    String sql = "UPDATE users SET islocked = ? WHERE username = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setBoolean(1, lockStatus);
      stmt.setString(2, username);
      int res = stmt.executeUpdate();
      ans = res > 0;
    } catch (SQLException e) {
    }
    return ans;
  }

  public boolean setUserRole(String username, String role) {
    boolean ans = false;
    String sql = "UPDATE users SET role = ? WHERE username = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, role);
      stmt.setString(2, username);
      int res = stmt.executeUpdate();
      ans = res > 0;
    } catch (SQLException e) {
    }
    return ans;
  }

  public java.util.List<User> getAllUsers() {
    java.util.List<User> ans = new java.util.ArrayList<>();
    String sql = "select * from users";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
          ResultSet rs = stmt.executeQuery();
          while (rs.next()) {
            String r = rs.getString("role");
            User u;
            if (r.equalsIgnoreCase("ADMIN")) u = new Admin();
            else if (r.equalsIgnoreCase("SELLER")) u = new Seller();
            else u = new Bidder();
            u.setId(rs.getInt("id"));
            u.setVersion(rs.getInt("version"));
            u.setUsername(rs.getString("username"));
            u.setFullName(rs.getString("fullname"));
            u.setPassword(rs.getString("password"));
            u.setEmail(rs.getString("email"));
            u.setAge(rs.getString("age"));
            u.setPhoneNumber(rs.getString("phonenumber"));
            u.setBalance(rs.getDouble("balance"));
            u.setMoneySpent(rs.getDouble("moneyspent"));
            u.setItemsBought(rs.getInt("itemsbought"));
            u.setMoneyReceived(rs.getDouble("moneyreceived"));
            u.setItemsSold(rs.getInt("itemssold"));
            u.setActive(rs.getBoolean("isactive"));
            u.setLocked(rs.getBoolean("islocked"));
            u.setAvatarUrl(rs.getString("avatar_url"));
            try {
              u.setAvgRating(rs.getDouble("avgrating"));
            } catch (Exception e) {
            }
            try {
              u.setTotalRatings(rs.getInt("totalratings"));
            } catch (Exception e) {
            }
            ans.add(u);
          }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return ans;
  }

  public boolean updateBalance(int id, double b) {
    boolean ans = false;
    String sql = "UPDATE users SET balance = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setDouble(1, b);
        ps.setInt(2, id);
        int res = ps.executeUpdate();
        ans = res > 0;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ans;
  }

  public boolean addBidderMetrics(int userId, double amount) {
    boolean ans = false;
    String sql =
            "UPDATE users SET moneyspent = moneyspent + ?, itemsbought = itemsbought + 1 WHERE id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setDouble(1, amount);
        ps.setInt(2, userId);
        int res = ps.executeUpdate();
        ans = res > 0;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ans;
  }

  public boolean addSellerMetrics(int userId, double amount) {
    boolean ans = false;
    String sql =
            "UPDATE users SET moneyreceived = moneyreceived + ?, itemssold = itemssold + 1 WHERE id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setDouble(1, amount);
        ps.setInt(2, userId);
        int res = ps.executeUpdate();
        ans = res > 0;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return ans;
  }

  public java.util.List<User> searchUsers(String keyword) {
    java.util.List<User> ans = new java.util.ArrayList<>();
    String sql =
        "select * from users where (lower(username) like ? or lower(fullname) like ?) and isactive = true and lower(role) != 'admin' limit 20";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
          PreparedStatement ps = conn.prepareStatement(sql)) {
          String kw = "%" + keyword.toLowerCase().trim() + "%";
          ps.setString(1, kw);
          ps.setString(2, kw);
          ResultSet rs = ps.executeQuery();
          while (rs.next()) {
            String r = rs.getString("role");
            User u;
            if (r.equalsIgnoreCase("ADMIN")) u = new Admin();
            else if (r.equalsIgnoreCase("SELLER")) u = new Seller();
            else u = new Bidder();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            u.setFullName(rs.getString("fullname"));
            u.setEmail(rs.getString("email"));
            u.setAvatarUrl(rs.getString("avatar_url"));
            try {
              u.setAvgRating(rs.getDouble("avgrating"));
            } catch (Exception e) {
            }
            try {
              u.setTotalRatings(rs.getInt("totalratings"));
            } catch (Exception e) {
            }
            u.setMoneySpent(rs.getDouble("moneyspent"));
            u.setItemsBought(rs.getInt("itemsbought"));
            u.setMoneyReceived(rs.getDouble("moneyreceived"));
            u.setItemsSold(rs.getInt("itemssold"));
            u.setPassword("");
            ans.add(u);
          }
    } catch (SQLException e) {
    }
    return ans;
  }

  public User getById(String id) {
    User ans = null;
    String sql = "SELECT * FROM users WHERE id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
          PreparedStatement ps = conn.prepareStatement(sql)){
          ps.setInt(1, Integer.parseInt(id));
          ResultSet rs = ps.executeQuery();
          if (rs.next()) {
            String r = rs.getString("role");
            if (r.equalsIgnoreCase("ADMIN")) ans = new Admin();
            else if (r.equalsIgnoreCase("SELLER")) ans = new Seller();
            else ans = new Bidder();
            ans.setId(rs.getInt("id"));
            ans.setVersion(rs.getInt("version"));
            ans.setUsername(rs.getString("username"));
            ans.setFullName(rs.getString("fullname"));
            ans.setPassword(rs.getString("password"));
            ans.setEmail(rs.getString("email"));
            ans.setAge(rs.getString("age"));
            ans.setPhoneNumber(rs.getString("phonenumber"));
            ans.setBalance(rs.getDouble("balance"));
            ans.setMoneySpent(rs.getDouble("moneyspent"));
            ans.setItemsBought(rs.getInt("itemsbought"));
            ans.setMoneyReceived(rs.getDouble("moneyreceived"));
            ans.setItemsSold(rs.getInt("itemssold"));
            ans.setActive(rs.getBoolean("isactive"));
            ans.setLocked(rs.getBoolean("islocked"));
            ans.setAvatarUrl(rs.getString("avatar_url"));
            try {
              ans.setAvgRating(rs.getDouble("avgrating"));
            } catch (Exception e) {
            }
            try {
              ans.setTotalRatings(rs.getInt("totalratings"));
            } catch (Exception e) {
            }
          }
    } catch (Exception e) {
    }
    return ans;
  }
}
