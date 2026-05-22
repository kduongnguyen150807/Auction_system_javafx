package com.auction.shared;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Binary header for UDP live video frames: itemId + userId + timestamp + JPEG payload. */
public final class LiveVideoPacket {
  public static final int HEADER_SIZE = 16;
  public static final int MAX_PAYLOAD = 60_000;

  private LiveVideoPacket() {}

  public static byte[] encode(int itemId, int userId, long timestampMs, byte[] jpeg) {
    byte[] out = new byte[HEADER_SIZE + jpeg.length];
    ByteBuffer buf = ByteBuffer.wrap(out).order(ByteOrder.BIG_ENDIAN);
    buf.putInt(itemId);
    buf.putInt(userId);
    buf.putLong(timestampMs);
    System.arraycopy(jpeg, 0, out, HEADER_SIZE, jpeg.length);
    return out;
  }

  public static int readItemId(byte[] packet) {
    return ByteBuffer.wrap(packet, 0, 4).order(ByteOrder.BIG_ENDIAN).getInt();
  }

  public static int readUserId(byte[] packet) {
    return ByteBuffer.wrap(packet, 4, 4).order(ByteOrder.BIG_ENDIAN).getInt();
  }

  public static long readTimestamp(byte[] packet) {
    return ByteBuffer.wrap(packet, 8, 8).order(ByteOrder.BIG_ENDIAN).getLong();
  }

  public static byte[] readJpeg(byte[] packet) {
    if (packet.length <= HEADER_SIZE) {
      return new byte[0];
    }
    byte[] jpeg = new byte[packet.length - HEADER_SIZE];
    System.arraycopy(packet, HEADER_SIZE, jpeg, 0, jpeg.length);
    return jpeg;
  }
}
