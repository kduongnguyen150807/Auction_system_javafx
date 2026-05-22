package com.auction.client.live;

/** Abstraction for live video transport (OCP). */
public interface VideoTransport {
  void start(int itemId, int userId, String serverHost, int udpPort);

  void stop();

  void setFrameListener(FrameListener listener);

  @FunctionalInterface
  interface FrameListener {
    void onFrame(int userId, byte[] jpegBytes);
  }
}
