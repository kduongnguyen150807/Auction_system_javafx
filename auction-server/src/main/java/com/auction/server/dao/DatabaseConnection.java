package com.auction.server.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

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

  private String url;
  private String user;
  private String password;

  /**
   * Khởi tạo và nạp cấu hình cơ sở dữ liệu.
   * <p>
   * Thực hiện nạp Driver MySQL và kiểm tra tính hợp lệ của cấu hình bằng cách
   * mở một kết nối thử nghiệm ngay khi khởi tạo instance.
   * </p>
   */
  private DatabaseConnection() {
    try {
      Properties props = new Properties();
      InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties");
      if (input != null) {
        props.load(input);
      } else {
        LOGGER.warn("db.properties not found on classpath, using defaults");
        props.setProperty("db.url", "jdbc:mysql://localhost:3306/auction_db");
        props.setProperty("db.user", "ba_nin");
        props.setProperty("db.password", "banin123");
      }

      this.url = props.getProperty("db.url");
      this.user = props.getProperty("db.user");
      this.password = props.getProperty("db.password");

      Class.forName("com.mysql.cj.jdbc.Driver");
      // Validate by opening a test connection
      Connection test = DriverManager.getConnection(url, user, password);
      test.close();
      LOGGER.info("Database connection validated successfully.");
    } catch (Exception e) {
      LOGGER.error("Failed to initialize database connection", e);
    }
  }

  /**
   * Trả về instance duy nhất của lớp {@code DatabaseConnection}.
   *
   * @return Đối tượng {@code DatabaseConnection} duy nhất (Singleton).
   */
  public static DatabaseConnection getInstance() {
    return Holder.INSTANCE;
  }

  /**
   * Tạo và trả về một kết nối {@link Connection} mới tới cơ sở dữ liệu.
   * <p>
   * Mỗi luồng xử lý (Thread) nên gọi phương thức này để nhận một kết nối riêng biệt,
   * đảm bảo tính độc lập dữ liệu giữa các phiên làm việc của Client.
   * </p>
   *
   * <pre>
   * // Cách sử dụng khuyến nghị:
   * try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
   *     // Thực hiện truy vấn SQL
   * } catch (SQLException e) {
   *     // Xử lý lỗi
   * }
   * </pre>
   *
   * @return Đối tượng {@link Connection} mới, hoặc {@code null} nếu không thể thiết lập kết nối.
   */
  public Connection getConnection() {
    try {
      return DriverManager.getConnection(url, user, password);
    } catch (Exception e) {
      LOGGER.error("Failed to create database connection", e);
      return null;
    }
  }
}