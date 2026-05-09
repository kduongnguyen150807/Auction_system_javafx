package com.auction.server;

import com.auction.server.controller.SocketServer;
import com.auction.server.dao.platform.DatabaseMigration;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
  public static void main(String[] args) {
    boolean ans = killport(8080);
    DatabaseMigration.runAll();
    SocketServer server = new SocketServer(8080);
    server.startServer();
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
      e.printStackTrace();
    }

    return ans;
  }
}