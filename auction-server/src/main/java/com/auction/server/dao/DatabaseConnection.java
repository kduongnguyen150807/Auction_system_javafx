package com.auction.server.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lớp {@code DatabaseConnection} quản lý việc kết nối tới cơ sở dữ liệu MySQL.
 *
 * <p>
 * Lớp này sử dụng mô hình <b>Bill Pugh Singleton</b> để đảm bảo tính duy nhất (thread-safe)
 * và cơ chế Lazy Initialization. Các thông số cấu hình được nạp từ file {@code db.properties}
 * trong classpath. Nếu không tìm thấy file, lớp sẽ tự động sử dụng cấu hình mặc định.
 * </p>
 * <p><b>Nguyên tắc sử dụng:</b> Phương thức {@link #getConnection()} trả về một đối tượng
 * {@link Connection} mới cho mỗi lần gọi. Người gọi (Caller) có trách nhiệm đóng kết nối
 * sau khi sử dụng bằng khối {@code try-with-resources} để tránh rò rỉ tài nguyên (resource leak).</p>
 */
public class DatabaseConnection {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnection.class);

  /**
   * Lớp lồng tĩnh (Static inner class) để triển khai Bill Pugh Singleton.
   * Cơ chế này đảm bảo instance chỉ được tạo ra khi được truy cập lần đầu.
   */
  private static final class Holder {
    static final DatabaseConnection INSTANCE = new DatabaseConnection();
  }

  private static final HikariDataSource ds;

  static {
    HikariConfig config = new HikariConfig();
    config.setDriverClassName("com.mysql.cj.jdbc.Driver");
    try (InputStream input =
           DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
      if (input != null) {
        Properties props = new Properties();
        props.load(input);

        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));

        config.setMaximumPoolSize(
          Integer.parseInt(props.getProperty("db.poolSize", "10"))
        );
      } else {
        LOGGER.warn("db.properties not found, using defaults");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/auction_db");
        config.setUsername("ba_nin");
        config.setPassword("banin123");
        config.setMaximumPoolSize(10);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to load db.properties", e);
    }
    ds = new HikariDataSource(config);
  }

  private DatabaseConnection() {}

  /**
   * Trả về instance duy nhất của lớp {@code DatabaseConnection}.
   *
   * @return Đối tượng {@code DatabaseConnection} duy nhất (Singleton).
   */
  public static DatabaseConnection getInstance() {
    return Holder.INSTANCE;
  }

  public Connection getConnection() {
    try {
      return ds.getConnection();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to connect to database", e);
    }
  }
}