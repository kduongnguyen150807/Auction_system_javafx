package com.auction.server.controller;

import com.auction.server.service.auction.SettlementService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketServer {
    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);
    private static final int DEFAULT_POOL_SIZE = 50;

    private final int port;
    private final ExecutorService pool;
    private volatile boolean running;
    private ServerSocket serverSocket;

    public SocketServer(int port) {
        this.port = port;
        this.pool = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
    }

    public void startServer() {
        running = true;
        SettlementService.getInstance().start();

        try (ServerSocket socket = new ServerSocket(this.port)) {
            this.serverSocket = socket;
            logger.info("server_started_on_port_{}", this.port);

            while (running) {
                Socket client = socket.accept();
                this.pool.execute(new ClientHandler(client));
            }
        } catch (IOException e) {
            if (running) {
                logger.error("socket_server_error", e);
            }
        } catch (Exception e) {
            logger.error("socket_server_error", e);
        } finally {
            shutdownPool();
        }
    }

    public void stopServer() {
        running = false;
        closeServerSocket();
        shutdownPool();
    }

    private void closeServerSocket() {
        if (serverSocket == null || serverSocket.isClosed()) {
            return;
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.error("error_closing_server_socket", e);
        }
    }

    private void shutdownPool() {
        pool.shutdown();
    }
}