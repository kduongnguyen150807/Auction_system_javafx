package com.auction.server.service.auction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.auction.server.dao.platform.DatabaseConnection;
import com.auction.server.dao.user.UserDao;
import com.auction.shared.User;
import com.zaxxer.hikari.HikariDataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link UserDao#login} session rules — no real database required. */
@ExtendWith(MockitoExtension.class)
class UserLoginSessionTest {

  private UserDao userDao;
  private Connection conn;
  private PreparedStatement selectPs;
  private PreparedStatement updatePs;
  private ResultSet rs;

  @BeforeEach
  void setup() throws SQLException {
    userDao = new UserDao();
    conn = mock(Connection.class);
    selectPs = mock(PreparedStatement.class);
    updatePs = mock(PreparedStatement.class);
    rs = mock(ResultSet.class);
    stubDatabasePoolReturns(conn);
    when(conn.prepareStatement(anyString()))
        .thenAnswer(
            inv -> {
              String sql = inv.getArgument(0, String.class);
              return sql.toUpperCase().startsWith("UPDATE") ? updatePs : selectPs;
            });
    when(selectPs.executeQuery()).thenReturn(rs);
    when(updatePs.executeUpdate()).thenReturn(1);
  }

  @Test
  void testLoginSuccess() throws SQLException {
    when(rs.next()).thenReturn(true);
    stubAdminRow(rs, 1, null);

    User user = userDao.login("admin", "123456");

    assertNotNull(user);
    assertEquals("admin", user.getUsername());
    assertNotNull(user.getSessionToken());
    assertTrue(user.getSessionToken().length() > 10);
  }

  @Test
  void testSingleDeviceLogin() throws SQLException {
    when(rs.next()).thenReturn(true);
    stubAdminRow(rs, 1, null);

    User firstLogin = userDao.login("admin", "123456");
    assertNotNull(firstLogin);

    when(rs.next()).thenReturn(true);
    stubAdminRow(rs, 1, "existing-session-token");

    User secondLogin = userDao.login("admin", "123456");
    assertNull(secondLogin);
  }

  @Test
  void testLogoutThenLoginAgain() throws SQLException {
    when(rs.next()).thenReturn(true);
    stubAdminRow(rs, 1, null);

    User user = userDao.login("admin", "123456");
    assertNotNull(user);

    boolean logout = userDao.clearSessionToken(user.getId());
    assertTrue(logout);

    when(rs.next()).thenReturn(true);
    stubAdminRow(rs, 1, null);

    User loginAgain = userDao.login("admin", "123456");
    assertNotNull(loginAgain);
  }

  private static void stubAdminRow(ResultSet rs, int id, String sessionToken) throws SQLException {
    when(rs.getString("role")).thenReturn("ADMIN");
    when(rs.getInt("id")).thenReturn(id);
    when(rs.getString("username")).thenReturn("admin");
    when(rs.getString("fullname")).thenReturn("Admin");
    when(rs.getString("password")).thenReturn("123456");
    when(rs.getString("email")).thenReturn("admin@test.com");
    when(rs.getString("age")).thenReturn("30");
    when(rs.getString("phone_number")).thenReturn("0900000000");
    when(rs.getDouble("balance")).thenReturn(0.0);
    when(rs.getDouble("money_spent")).thenReturn(0.0);
    when(rs.getInt("items_bought")).thenReturn(0);
    when(rs.getDouble("money_received")).thenReturn(0.0);
    when(rs.getInt("items_sold")).thenReturn(0);
    when(rs.getDouble("avg_rating")).thenReturn(0.0);
    when(rs.getInt("total_ratings")).thenReturn(0);
    when(rs.getBoolean("is_active")).thenReturn(true);
    when(rs.getBoolean("is_locked")).thenReturn(false);
    when(rs.getString("avatar_url")).thenReturn(null);
    when(rs.getString("session_token")).thenReturn(sessionToken);
    when(rs.getTimestamp("last_login_at")).thenReturn(null);
  }

  private static final class StubHikariPool extends HikariDataSource {
    private final Connection connection;

    StubHikariPool(Connection connection) {
      this.connection = connection;
    }

    @Override
    public Connection getConnection() {
      return connection;
    }

    @Override
    public Connection getConnection(String username, String password) {
      return connection;
    }
  }

  private static void stubDatabasePoolReturns(Connection conn) {
    try {
      DatabaseConnection dc = DatabaseConnection.getInstance();
      Field dsField = DatabaseConnection.class.getDeclaredField("datasource");
      dsField.setAccessible(true);
      dsField.set(dc, new StubHikariPool(conn));
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }
}
