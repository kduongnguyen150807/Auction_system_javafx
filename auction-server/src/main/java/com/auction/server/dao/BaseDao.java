package com.auction.server.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseDao<T> {
  private static final Logger LOGGER = Logger.getLogger(BaseDao.class.getName());

  protected Connection getConn() {
    return DatabaseConnection.getInstance().getConnection();
  }

  protected abstract T mapRow(ResultSet rs) throws SQLException;

  protected List<T> queryList(String sql, Object... params) {
    List<T> results = new ArrayList<>();
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      setParameters(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) results.add(mapRow(rs));
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Query failed: " + sql, e);
    }
    return results;
  }

  protected T querySingle(String sql, Object... params) {
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      setParameters(ps, params);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return mapRow(rs);
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Query failed: " + sql, e);
    }
    return null;
  }

  protected boolean executeUpdate(String sql, Object... params) {
    try (Connection conn = getConn(); PreparedStatement ps = conn.prepareStatement(sql)) {
      setParameters(ps, params);
      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Update failed: " + sql, e);
      return false;
    }
  }

  private void setParameters(PreparedStatement ps, Object... params) throws SQLException {
    for (int i = 0; i < params.length; i++) {
      Object param = params[i];
      if (param instanceof String) ps.setString(i + 1, (String) param);
      else if (param instanceof Integer) ps.setInt(i + 1, (Integer) param);
      else if (param instanceof Double) ps.setDouble(i + 1, (Double) param);
      else if (param instanceof Boolean) ps.setBoolean(i + 1, (Boolean) param);
      else if (param instanceof Timestamp) ps.setTimestamp(i + 1, (Timestamp) param);
      else if (param == null) ps.setNull(i + 1, Types.NULL);
      else ps.setObject(i + 1, param);
    }
  }
}
