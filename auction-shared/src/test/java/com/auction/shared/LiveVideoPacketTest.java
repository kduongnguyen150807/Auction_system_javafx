package com.auction.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LiveVideoPacket")
class LiveVideoPacketTest {

  @Test
  void encodeDecode_roundTrip() {
    byte[] jpeg = new byte[] {0x01, 0x02, (byte) 0xFF};
    byte[] packet = LiveVideoPacket.encode(42, 7, 1_234_567L, jpeg);

    assertEquals(LiveVideoPacket.HEADER_SIZE + jpeg.length, packet.length);
    assertEquals(42, LiveVideoPacket.readItemId(packet));
    assertEquals(7, LiveVideoPacket.readUserId(packet));
    assertEquals(1_234_567L, LiveVideoPacket.readTimestamp(packet));
    assertArrayEquals(jpeg, LiveVideoPacket.readJpeg(packet));
  }

  @Test
  void readJpeg_emptyWhenHeaderOnly() {
    byte[] packet = LiveVideoPacket.encode(1, 2, 0L, new byte[0]);
    assertEquals(0, LiveVideoPacket.readJpeg(packet).length);
  }
}
