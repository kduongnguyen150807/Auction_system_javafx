package com.auction.client.live;

import com.auction.shared.LiveVideoPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Sends webcam JPEG frames via UDP and receives relayed peer frames. */
public final class UdpRelayVideoTransport implements VideoTransport {
  private static final Logger logger = LoggerFactory.getLogger(UdpRelayVideoTransport.class);

  private final AtomicBoolean running = new AtomicBoolean(false);
  private Thread captureThread;
  private Thread receiveThread;
  private DatagramSocket socket;
  private FrameListener listener;
  private StatusListener statusListener;
  private int itemId;
  private int userId;
  private InetAddress serverAddress;
  private int udpPort;

  @Override
  public void setFrameListener(FrameListener listener) {
    this.listener = listener;
  }

  public void setStatusListener(StatusListener statusListener) {
    this.statusListener = statusListener;
  }

  @Override
  public synchronized void start(int itemId, int userId, String serverHost, int udpPort) {
    stop();
    this.itemId = itemId;
    this.userId = userId;
    this.udpPort = udpPort;
    try {
      this.serverAddress = InetAddress.getByName(serverHost);
      socket = new DatagramSocket();
      socket.setReceiveBufferSize(256 * 1024);
      running.set(true);
      notifyStatus("Đang kết nối video UDP → " + serverHost + ":" + udpPort);
      receiveThread = new Thread(this::receiveLoop, "udp-video-recv");
      receiveThread.setDaemon(true);
      receiveThread.start();
      captureThread = new Thread(this::captureLoop, "udp-video-cap");
      captureThread.setDaemon(true);
      captureThread.start();
    } catch (Exception e) {
      logger.warn("UDP video transport failed to start: {}", e.getMessage());
      notifyStatus("Lỗi video: " + e.getMessage());
      stop();
    }
  }

  @Override
  public synchronized void stop() {
    running.set(false);
    if (socket != null && !socket.isClosed()) {
      socket.close();
    }
    socket = null;
    captureThread = null;
    receiveThread = null;
  }

  private void captureLoop() {
    boolean usingWebcam = false;
    try (WebcamFrameGrabber grabber = new WebcamFrameGrabber()) {
      usingWebcam = grabber.open();
      if (usingWebcam) {
        notifyStatus("Webcam đang phát — cổng UDP " + udpPort);
      } else {
        notifyStatus("Không có webcam — dùng khung hình demo");
      }

      int placeholderEvery = 0;
      while (running.get() && socket != null && !socket.isClosed()) {
        byte[] jpeg;
        if (usingWebcam) {
          var frame = grabber.grabFrame();
          if (frame == null) {
            Thread.sleep(40);
            continue;
          }
          jpeg = LiveVideoFrameEncoder.toJpeg(frame);
        } else {
          placeholderEvery++;
          jpeg =
              LiveVideoFrameEncoder.placeholderFrame(
                  "Demo frame " + (placeholderEvery % 100) + " — user " + userId);
          Thread.sleep(200);
        }

        if (jpeg.length == 0 || jpeg.length > LiveVideoPacket.MAX_PAYLOAD) {
          continue;
        }
        publishFrame(jpeg);
        Thread.sleep(usingWebcam ? 80 : 200);
      }
    } catch (Exception e) {
      if (running.get()) {
        logger.warn("Capture loop ended: {}", e.getMessage());
        notifyStatus("Video dừng: " + e.getMessage());
      }
    }
  }

  private void publishFrame(byte[] jpeg) throws IOException {
    byte[] packet = LiveVideoPacket.encode(itemId, userId, System.currentTimeMillis(), jpeg);
    DatagramPacket out = new DatagramPacket(packet, packet.length, serverAddress, udpPort);
    socket.send(out);

    // Local preview — always show own feed even without a second participant.
    deliverFrame(userId, jpeg);
  }

  private void receiveLoop() {
    byte[] buf = new byte[LiveVideoPacket.HEADER_SIZE + LiveVideoPacket.MAX_PAYLOAD];
    while (running.get() && socket != null && !socket.isClosed()) {
      try {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        int len = packet.getLength();
        if (len <= LiveVideoPacket.HEADER_SIZE) {
          continue;
        }
        byte[] data = java.util.Arrays.copyOf(packet.getData(), len);
        int frameItemId = LiveVideoPacket.readItemId(data);
        int frameUserId = LiveVideoPacket.readUserId(data);
        if (frameItemId != itemId) {
          continue;
        }
        if (frameUserId == userId) {
          continue;
        }
        byte[] jpeg = LiveVideoPacket.readJpeg(data);
        if (jpeg.length > 0) {
          deliverFrame(frameUserId, jpeg);
        }
      } catch (IOException e) {
        if (running.get()) {
          logger.debug("UDP receive ended: {}", e.getMessage());
        }
      }
    }
  }

  private void deliverFrame(int frameUserId, byte[] jpeg) {
    FrameListener l = listener;
    if (l != null && jpeg.length > 0) {
      l.onFrame(frameUserId, jpeg);
    }
  }

  private void notifyStatus(String message) {
    StatusListener s = statusListener;
    if (s != null) {
      s.onStatus(message);
    }
  }

  /** Optional hook for UI status text. */
  @FunctionalInterface
  public interface StatusListener {
    void onStatus(String message);
  }
}
