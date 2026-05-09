package com.auction.server.controller;

import com.auction.server.utils.RequestDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lớp {@code SocketServer} chịu trách nhiệm khởi tạo và quản lý kết nối mạng cho hệ
 * Lớp sử dụng một {@link ServerSocket} để lắng nghe kết nối từ client và sử dụng một
 * {@link ExecutorService} ThreadPool để sử lý đa luồng, phục vụ nhiều client cùng lúc
 */
public class SocketServer {
  private static final Logger LOGGER = LoggerFactory.getLogger(SocketServer.class);

  private final RequestDispatcher requestDispatcher;

  private final int port;
  private final ExecutorService pool;

  /**
   * khởi tạo một SocketServer tại cổng chỉ định
   * @param port Cổng dịch vụ mà server liên kết
   */
  public SocketServer(int port, RequestDispatcher requestDispatcher) {
    this.port = port;
    this.pool = Executors.newFixedThreadPool(50);
    this.requestDispatcher = requestDispatcher;
  }

  /**
   * Bắt đầu chạy server
   * Đăng ký các handler vào requestDispatcher chung
   *<p>
   * Phương thức mở một {@link ServerSocket} và đi vào vòng lặp để chấp nhận kết nối từ client
   * Mỗi khi có một kết nối mới, một {@code ClientHandler} sẽ được tạo để chứa socket đó rồi đưa vào ThreadPool để xử
   *</p>
   * @throws IOException Nếu xảy ra lỗi khi mở cổng hoặc chấp nhận kết nối.
   */
  public void start() {
    try {
      ServerSocket serverSocket = new ServerSocket(port);
      LOGGER.info("Server started on port {}", port);

      while (true) {
        Socket client = serverSocket.accept();
        ClientHandler handler = new ClientHandler(client, requestDispatcher);
        this.pool.execute(handler);
      }
    } catch (IOException e) {
      LOGGER.error("Error while starting server", e);
    }
  }
}
