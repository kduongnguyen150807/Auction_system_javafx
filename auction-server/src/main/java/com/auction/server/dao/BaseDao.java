package com.auction.server.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDao {
  private static final Logger LOGGER = LoggerFactory.getLogger(BaseDao.class);

  protected boolean update(Connection connection, String sql, List<Object> params) {
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      setParams(ps, params);

      int res = ps.executeUpdate();
      return res > 0;
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  protected <T> List<T> query(Connection connection, String sql, List<Object> params, ResultSetMapper<T> mapper) {
    List<T> results = new ArrayList<>();
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      setParams(ps, params);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapper.map(rs));
        }
      }
    } catch (SQLException e) {
      LOGGER.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }

    return results;
  }

  void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
    if  (params == null) return;
    for (int i = 0; i < params.size(); i++) {
      ps.setObject(i + 1, params.get(i));
    }
  }
}



