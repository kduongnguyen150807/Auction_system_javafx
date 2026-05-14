package com.auction.server.dao.transaction;

import com.auction.server.dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public class TransactionManager {
  /* write with rollback */
  public static <T> T execute(TransactionCallback<T> callback) {
    Connection conn = null;
    try {
      conn = DatabaseConnection.getInstance().getConnection();
      conn.setAutoCommit(false);
      T result = callback.doInTransaction(conn);
      conn.commit();
      return result;
    } catch (RuntimeException e) {
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
      throw e;
    } catch (Exception e) {
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException ex) {}
      }
      throw new RuntimeException(e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
    }
  }


  /* read */
  public static <T> T read(Function<Connection, T> callback) {
    try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
      return callback.apply(conn);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}