package com.auction.server.controller;

import com.auction.server.context.HandlerContext;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.Response;

import com.auction.server.utils.RequestDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Lớp {@code ClientHandler} chịu trách nhiệm quản lý phiên làm việc của một Client duy nhất.
 * <p>
 * Mỗi instance của lớp này chạy trên một luồng riêng biệt, thực hiện việc lắng nghe liên tục các
 * đối tượng {@link Request} từ Client, điều phối chúng thông qua {@link RequestDispatcher}
 * và phản hồi kết quả.
 * </p>
 *
 * Luồng hoạt động: Khởi tạo Stream -> Lắng nghe (Loop) -> Điều phối -> Phản hồi -> Dọn dẹp.
 *
 * @see RequestDispatcher
 */
public class ClientHandler implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

  private final HandlerContext handlerContext;
  private final RequestDispatcher requestDispatcher;
  private final Socket socket;
  private ObjectOutputStream out;
  private ObjectInputStream in;

  /**
   * Khởi tạo một Handler mới cho một kết nối Socket cụ thể.
   * <p>
   * Constructor này sẽ thiết lập các luồng I/O và lưu {@code requestDispatcher} vào thuộc tính riêng
   * </p>
   *
   * @param socket Kết nối socket đã được chấp nhận từ {@code ServerSocket}.
   * @param requestDispatcher Bộ điều phối các yêu cầu dùng chung của hệ thống.
   */
  public ClientHandler(Socket socket, RequestDispatcher requestDispatcher) {
    this.socket = socket;
    this.requestDispatcher = requestDispatcher;
    this.handlerContext = new HandlerContext(this);
    try {
      this.out = new ObjectOutputStream(this.socket.getOutputStream());
      this.out.flush();
      this.in = new ObjectInputStream(this.socket.getInputStream());
    } catch (Exception e) {
      LOGGER.error("Failed to initialize client handler streams", e);
    }
  }

  /**
   * Điểm bắt đầu thực thi của luồng (Thread).
   * <p>
   * Thực hiện khởi chạy vòng lặp lắng nghe. Các ngoại lệ ngắt kết nối thông thường (EOF)
   * sẽ được log ở mức INFO, các lỗi không mong muốn sẽ được log ở mức WARN.
   * Tài nguyên luôn được giải phóng ở khối {@code finally}.
   * </p>
   */
  @Override
  public void run() {
    try {
      startListener();
    } catch (EOFException e) {
      LOGGER.info("client disconnected");
    } catch (Exception e) {
      LOGGER.warn("Unexpected exception in listener loop", e);
    } finally {
      cleanup();
    }
  }

  /**
   * Vòng lặp chính lắng nghe dữ liệu từ Socket.
   * <p>
   * Chạy cho đến khi luồng bị ngắt (interrupt) hoặc kết nối bị đóng từ phía Client.
   * </p>
   *
   * @throws IOException Nếu xảy ra lỗi truyền tải dữ liệu.
   * @throws ClassNotFoundException Nếu đối tượng nhận được không khớp với định nghĩa lớp trong hệ thống.
   */
  private void startListener() throws IOException, ClassNotFoundException {
    while (!Thread.currentThread().isInterrupted()) {
      Request<?> request = (Request<?>) in.readObject();
      handleRequest(request);
    }
  }

  /**
   * Gửi đối tượng phản hồi về phía Client.
   * Phương thức này được đồng bộ hóa để đảm bảo an toàn khi nhiều luồng
   * cùng muốn gửi dữ liệu về một Client
   *
   * @param response Đối tượng phản hồi cần gửi.
   */
  private void sendResponse(Response<?> response) {
    synchronized (out) {
      try {
        out.reset();
        out.writeObject(response);
        out.flush();
        LOGGER.debug("Sent response of type: {}", response.toString());
      } catch (IOException e) {
        LOGGER.error("Failed to send response to client: {}", e.getMessage());
      }
    }
  }

  /**
   * Chuyển tiếp yêu cầu tới bộ điều phối và quản lý phản hồi.
   *
   * @param request Đối tượng chứa thông tin yêu cầu từ Client.
   */
  private void handleRequest(Request<?> request) {
    Response<?> response = requestDispatcher.dispatch(request, handlerContext);
    sendResponse(response);
  }

  /**
   * Đóng Socket và giải phóng tài nguyên hệ thống.
   * <p>
   * Phương thức này đảm bảo không để lại các "zombie socket" khi client ngắt kết nối
   * hoặc server xảy ra lỗi.
   * </p>
   */
  private void cleanup() {
    try {
      if (this.socket != null && !this.socket.isClosed()) {
        this.socket.close();
      }
    } catch (IOException e) {
      LOGGER.debug("Error closing socket", e);
    }
  }
}
