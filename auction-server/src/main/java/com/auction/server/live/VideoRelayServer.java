package com.auction.server.live;

import com.auction.shared.LiveVideoPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** UDP server that relays JPEG video frames between live session participants. */
public final class VideoRelayServer implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(VideoRelayServer.class);
  private static volatile VideoRelayServer instance;

  private final int port;
  private DatagramSocket socket;
  private volatile boolean running;

  /** itemId -> (userId -> address) */
  private final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, InetSocketAddress>> routes =
      new ConcurrentHashMap<>();

  private VideoRelayServer(int port) {
    this.port = port;
  }

  public static VideoRelayServer getInstance() {
    if (instance == null) {
      synchronized (VideoRelayServer.class) {
        if (instance == null) {
          String env = System.getenv("UDP_VIDEO_PORT");
          int p = (env != null && !env.isBlank()) ? Integer.parseInt(env.trim()) : 9090;
          instance = new VideoRelayServer(p);
        }
      }
    }
    return instance;
  }

  public int getPort() {
    return port;
  }

  public void start() {
    if (running) {
      return;
    }
    running = true;
    Thread t = new Thread(this, "udp-video-relay");
    t.setDaemon(true);
    t.start();
  }

  public void stop() {
    running = false;
    if (socket != null && !socket.isClosed()) {
      socket.close();
    }
  }

  public void unregisterUser(int itemId, int userId) {
    ConcurrentHashMap<Integer, InetSocketAddress> map = routes.get(itemId);
    if (map != null) {
      map.remove(userId);
      if (map.isEmpty()) {
        routes.remove(itemId, map);
      }
    }
  }

  @Override
  public void run() {
    try {
      socket = new DatagramSocket(port);
      logger.info("UDP video relay listening on port {}", port);
      byte[] buf = new byte[LiveVideoPacket.HEADER_SIZE + LiveVideoPacket.MAX_PAYLOAD];
      while (running) {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
          socket.receive(packet);
          handlePacket(packet);
        } catch (IOException e) {
          if (running) {
            logger.debug("UDP receive error: {}", e.getMessage());
          }
        }
      }
    } catch (SocketException e) {
      logger.error("Failed to bind UDP port {}", port, e);
    } finally {
      if (socket != null) {
        socket.close();
      }
    }
  }

  private void handlePacket(DatagramPacket packet) {
    int len = packet.getLength();
    if (len <= LiveVideoPacket.HEADER_SIZE) {
      return;
    }
    byte[] data = packet.getData();
    int itemId = LiveVideoPacket.readItemId(data);
    int userId = LiveVideoPacket.readUserId(data);

    if (!LiveSessionManager.getInstance().isParticipant(itemId, userId)) {
      return;
    }

    InetSocketAddress sender = new InetSocketAddress(packet.getAddress(), packet.getPort());
    routes
        .computeIfAbsent(itemId, k -> new ConcurrentHashMap<>())
        .put(userId, sender);

    ConcurrentHashMap<Integer, InetSocketAddress> peers = routes.get(itemId);
    if (peers == null) {
      return;
    }
    Set<Integer> allowed = LiveSessionManager.getInstance().getParticipants(itemId);
    for (Map.Entry<Integer, InetSocketAddress> entry : peers.entrySet()) {
      if (!allowed.contains(entry.getKey())) {
        continue;
      }
      InetSocketAddress target = entry.getValue();
      if (target == null) {
        continue;
      }
      try {
        DatagramPacket out = new DatagramPacket(data, len, target);
        socket.send(out);
      } catch (IOException e) {
        logger.debug("UDP relay failed to {}: {}", target, e.getMessage());
      }
    }
  }
}
