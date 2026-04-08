package com.auction.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.auction.server.service.AuctionCloser;
import com.auction.server.controller.SocketServer;

@SpringBootApplication
public class Main {
  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
    System.out.println("✅ Spring Boot Server (Chat) đã sẵn sàng!");
    new AuctionCloser().start();
    SocketServer server = new SocketServer(8080);
    server.startServer();
    System.out.println("✅ Socket Auction Server đã sẵn sàng trên cổng 8080!");
  }
}