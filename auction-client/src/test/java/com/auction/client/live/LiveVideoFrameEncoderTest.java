package com.auction.client.live;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LiveVideoFrameEncoder")
class LiveVideoFrameEncoderTest {

  @Test
  void toJpeg_producesNonEmptyBytes() throws Exception {
    BufferedImage frame = new BufferedImage(160, 120, BufferedImage.TYPE_INT_ARGB);
    byte[] jpeg = LiveVideoFrameEncoder.toJpeg(frame);
    assertNotNull(jpeg);
    assertTrue(jpeg.length > 100);
    assertEquals((byte) 0xFF, jpeg[0]);
    assertEquals((byte) 0xD8, jpeg[1]);
  }

  @Test
  void placeholderFrame_producesJpeg() throws Exception {
    byte[] jpeg = LiveVideoFrameEncoder.placeholderFrame("test");
    assertTrue(jpeg.length > 50);
  }
}
