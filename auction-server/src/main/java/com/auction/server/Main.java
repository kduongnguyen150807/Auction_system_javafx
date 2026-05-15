package com.auction.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.auction.server.service.AuctionCloser;
import com.auction.server.controller.SocketServer;
import com.auction.server.controller.SocketServer;
import com.auction.server.dao.platform.DatabaseMigration;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
    System.out.println("✅ Spring Boot Server (Chat) đã sẵn sàng!");
    new AuctionCloser().start();
    SocketServer server = new SocketServer(8080);
    String portstr = System.getenv("SERVER_PORT");
    int port = (portstr != null && !portstr.trim().isEmpty()) ? Integer.parseInt(portstr) : 8080;

    boolean ans = killport(port);
    DatabaseMigration.runAll();
    server.startServer();
    System.out.println("✅ Socket Auction Server đã sẵn sàng trên cổng 8080!");
  }

  private static boolean killport(int port) {
    boolean ans = false;
    String osname = System.getProperty("os.name").toLowerCase();

    try {
      if (osname.contains("win")) {
        String command = "cmd /c netstat -ano | findstr :" + port;
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.contains("LISTENING")) {
            String[] parts = line.trim().split("\\s+");
            String pid = parts[parts.length - 1];
            Runtime.getRuntime().exec("taskkill /F /PID " + pid).waitFor();
            ans = true;
          }
        }
      } else {
        String[] command = {"/bin/sh", "-c", "lsof -t -i :" + port};
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String pid;
        while ((pid = reader.readLine()) != null) {
          if (!pid.trim().isEmpty()) {
            Runtime.getRuntime().exec("kill -9 " + pid).waitFor();
            ans = true;
          }
        }
      }
    } catch (Exception e) {
      logger.error("error killing port", e);
    }

    return ans;
  }
}