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

  protected Connection getConnection() {
    return DatabaseConnection.getInstance().getConnection();
  }

  protected boolean update(String sql, List<Object> params) {
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      if (params != null) {
        for (int i = 0; i < params.size(); i++) {
          ps.setObject(i + 1, params.get(i));
        }
      }

      int affectedRows = ps.executeUpdate();
      LOGGER.info("Execute update thành công. Số dòng bị ảnh hưởng: {}", affectedRows);
      return affectedRows > 0;

    } catch (SQLException e) {
      LOGGER.error("Lỗi khi thực thi lệnh UPDATE: {}", sql, e);
      return false;
    }
  }

  protected <T> List<T> query(String sql, List<Object> params, ResultSetMapper<T> mapper) {
    List<T> results = new ArrayList<>();
    try (Connection conn = getConnection()) {
      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        if (params != null) {
          for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
          }
        }

        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            results.add(mapper.map(rs));
          }
        } catch (SQLException e) {
          LOGGER.error("Error while executing query", e);
        }
      }
    } catch (SQLException e) {
      LOGGER.error("Error while executing query", e);
    }
    return results;
  }
}



