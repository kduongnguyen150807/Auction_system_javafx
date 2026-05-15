package com.auction.server.service.auction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.platform.DatabaseConnection;
import com.auction.server.dao.user.UserDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.shared.BidTransaction;
import com.zaxxer.hikari.HikariDataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractAuctionManagerMockingTest {

  @Mock protected ItemDao itemDao;
  @Mock protected UserDao userDao;
  @Mock protected BidDao bidDao;
  @Mock protected TransactionLogDao logDao;

  protected Connection jdbcConn;

  @BeforeEach
  void installJdbcPoolStubAndLeaderboardEmptyUsers() {
    jdbcConn = mock(Connection.class);
    stubDatabasePoolReturns(jdbcConn);
    when(userDao.getAllUsers()).thenReturn(Collections.emptyList());
  }

  @AfterEach
  void clearAuctionManagerSingleton() {
    AuctionManager.resetForTest();
  }

  protected final void bindAuctionManagerSingleton(AuctionManager manager) {
    try {
      AuctionManager.resetForTest();
      Field f = AuctionManager.class.getDeclaredField("instance");
      f.setAccessible(true);
      f.set(null, manager);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private static final class StubHikariPool extends HikariDataSource {
    private final Connection connection;

    StubHikariPool(Connection connection) {
      this.connection = connection;
    }

    @Override
    public Connection getConnection() throws SQLException {
      return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
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

  protected void stubSuccessfulEnglishBid(int itemId, int bidderId, double bidAmount, int prevHighestBidderId) {
    try {
      when(userDao.deductBalanceTx(eq(bidderId), eq(bidAmount), eq(jdbcConn))).thenReturn(true);
      when(logDao.insertLogTx(eq(bidderId), eq("BID_HOLD"), eq(-bidAmount), eq(itemId), eq(jdbcConn))).thenReturn(true);
      when(bidDao.getCurrentHighestBidderTx(eq(itemId), eq(jdbcConn))).thenReturn(prevHighestBidderId);
      lenient().when(bidDao.placeBidTx(any(BidTransaction.class), eq(jdbcConn))).thenReturn(true);

      // FIX: Thêm anyInt() cho tham số version của Optimistic Locking
      when(itemDao.updatePriceTx(eq(itemId), eq(bidAmount), anyInt(), eq(jdbcConn))).thenReturn(true);

      if (prevHighestBidderId > 0) {
        when(userDao.creditBalanceTx(eq(prevHighestBidderId), anyDouble(), eq(jdbcConn))).thenReturn(true);
        when(logDao.insertLogTx(eq(prevHighestBidderId), eq("BID_REFUND"), anyDouble(), eq(itemId), eq(jdbcConn)))
                .thenReturn(true);
      }
    } catch (SQLException e) {
      throw new AssertionError(e);
    }
  }
}