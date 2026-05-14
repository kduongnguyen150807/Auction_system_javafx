package com.auction.client.network;

import com.auction.shared.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quản lý kết nối TCP thô và vòng lặp đọc dữ liệu JSON.
 * Tuân thủ Google Coding Convention.
 */
final class ObjectSocketConnection {

  private static final Logger logger = LoggerFactory.getLogger(ObjectSocketConnection.class);
  private static final int MAX_PACKET_SIZE = 10 * 1024 * 1024; // Giới hạn 10MB tránh OOM

  private final Socket socket;
  private final DataOutputStream out;
  private final DataInputStream in;
  private final ObjectMapper jsonMapper;

  private ObjectSocketConnection(Socket socket) throws IOException {
    this.socket = socket;
    this.out = new DataOutputStream(socket.getOutputStream());
    this.in = new DataInputStream(socket.getInputStream());
    this.jsonMapper = new ObjectMapper();
    this.jsonMapper.registerModule(new JavaTimeModule());
  }

  static ObjectSocketConnection connect(String host, int port) throws IOException {
    Socket socket = new Socket(host, port);
    socket.setKeepAlive(true);
    socket.setTcpNoDelay(true); // Giảm độ trễ cho các gói tin nhỏ
    return new ObjectSocketConnection(socket);
  }

  DataOutputStream getOut() {
    return out;
  }

  Socket getSocket() {
    return socket;
  }

  /**
   * Vòng lặp đọc dữ liệu chạy ngầm.
   * Giao thức: [4 bytes độ dài][Chuỗi JSON UTF-8]
   */
  void startReadLoop(Consumer<Response> onResponse, Consumer<Throwable> onDisconnect) {
    Thread listenerThread = new Thread(() -> {
      try {
        while (!socket.isClosed()) {
          // 1. Đọc độ dài gói tin
          int length = in.readInt();
          if (length <= 0 || length > MAX_PACKET_SIZE) {
            throw new IOException("Kích thước gói tin không hợp lệ: " + length);
          }

          // 2. Đọc nội dung JSON dựa trên độ dài
          byte[] payload = new byte[length];
          in.readFully(payload);

          // 3. Giải mã JSON thành Response Object
          String jsonStr = new String(payload, StandardCharsets.UTF_8);
          Response response = jsonMapper.readValue(jsonStr, Response.class);

          if (response != null) {
            onResponse.accept(response);
          }
        }
      } catch (EOFException e) {
        logger.info("Server đã ngắt kết nối (EOF).");
        onDisconnect.accept(e);
      } catch (Exception e) {
        logger.error("Lỗi vòng lặp đọc: {}", e.getMessage());
        onDisconnect.accept(e);
      }
    }, "Network-Listener-Thread");

    listenerThread.setDaemon(true);
    listenerThread.start();
  }
}