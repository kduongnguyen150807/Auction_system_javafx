package com.auction.server.dao.platform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseDao<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(BaseDao.class);

  protected Connection getConn() {
    return DatabaseConnection.getInstance().getConnection();
  }

  protected abstract T mapRow(ResultSet rs) throws SQLException;

  protected List<T> queryList(String sql, Object... params) {
    List<T> results = new ArrayList<>();

    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      setParameters(ps, params);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapRow(rs));
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Query list failed. SQL={}", sql, e);
    }

    return results;
  }

  protected T querySingle(String sql, Object... params) {
    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      setParameters(ps, params);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapRow(rs);
        }
      }
    } catch (Exception e) {
      LOGGER.warn("Query single failed. SQL={}", sql, e);
    }

    return null;
  }

  protected boolean executeUpdate(String sql, Object... params) {
    try (Connection conn = getConn();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      setParameters(ps, params);
      return ps.executeUpdate() > 0;
    } catch (Exception e) {
      LOGGER.warn("Update failed. SQL={}", sql, e);
      return false;
    }
  }

  private void setParameters(PreparedStatement ps, Object... params) throws SQLException {
    for (int i = 0; i < params.length; i++) {
      setParameter(ps, i + 1, params[i]);
    }
  }

  private void setParameter(PreparedStatement ps, int index, Object param) throws SQLException {
    if (param == null) {
      ps.setNull(index, Types.NULL);
    } else if (param instanceof String value) {
      ps.setString(index, value);
    } else if (param instanceof Integer value) {
      ps.setInt(index, value);
    } else if (param instanceof Long value) {
      ps.setLong(index, value);
    } else if (param instanceof Double value) {
      ps.setDouble(index, value);
    } else if (param instanceof Boolean value) {
      ps.setBoolean(index, value);
    } else if (param instanceof Timestamp value) {
      ps.setTimestamp(index, value);
    } else if (param instanceof LocalDateTime value) {
      ps.setTimestamp(index, Timestamp.valueOf(value));
    } else {
      ps.setObject(index, param);
    }
  }
}