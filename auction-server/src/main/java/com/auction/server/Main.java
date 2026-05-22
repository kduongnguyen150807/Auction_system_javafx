package com.auction.server;

import com.auction.server.controller.SocketServer;
import com.auction.server.dao.platform.DatabaseConnection;
import com.auction.server.dao.platform.DatabaseMigration;
import com.auction.server.live.VideoRelayServer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    String portstr = System.getenv("SERVER_PORT");
    int port = (portstr != null && !portstr.trim().isEmpty()) ? Integer.parseInt(portstr) : 8080;

    killport(port);
    int udpPort = resolveUdpPort();
    killport(udpPort);
    DatabaseMigration.runAll();

    // TÍNH NĂNG 4: GRACEFUL SHUTDOWN HOOK
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      logger.info("=== HỆ THỐNG ĐANG TẮT (GRACEFUL SHUTDOWN) ===");
      VideoRelayServer.getInstance().stop();
      try {
        DatabaseConnection.getInstance().closePool();
      } catch (Exception e) {
        logger.error("Lỗi khi đóng Database Pool", e);
      }
      logger.info("=== SERVER ĐÃ TẮT HOÀN TOÀN ===");
    }));

    SocketServer server = new SocketServer(port);
    VideoRelayServer.getInstance().start();
    server.startServer();
  }

  private static int resolveUdpPort() {
    String env = System.getenv("UDP_VIDEO_PORT");
    return (env != null && !env.isBlank()) ? Integer.parseInt(env.trim()) : 9090;
  }

  private static void killport(int port) {
    String osname = System.getProperty("os.name").toLowerCase();
    try {
      ProcessBuilder pb;
      if (osname.contains("win")) {
        pb = new ProcessBuilder("cmd", "/c", "netstat -ano | findstr :" + port);
      } else {
        pb = new ProcessBuilder("sh", "-c", "lsof -t -i :" + port);
      }

      Process process = pb.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        if (osname.contains("win") && line.contains("LISTENING")) {
          String[] parts = line.trim().split("\\s+");
          String pid = parts[parts.length - 1];
          new ProcessBuilder("taskkill", "/F", "/PID", pid).start().waitFor();
        } else if (!osname.contains("win") && !line.trim().isEmpty()) {
          new ProcessBuilder("kill", "-9", line.trim()).start().waitFor();
        }
      }
    } catch (Exception e) {
      logger.error("Error killing port {}", port, e);
    }
  }
}