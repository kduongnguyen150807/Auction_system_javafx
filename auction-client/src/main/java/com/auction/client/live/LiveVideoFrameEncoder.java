package com.auction.client.live;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/** Encodes frames as JPEG for UDP transport. */
final class LiveVideoFrameEncoder {
  private static final int WIDTH = 320;
  private static final int HEIGHT = 240;

  private LiveVideoFrameEncoder() {}

  static byte[] toJpeg(BufferedImage source) throws IOException {
    BufferedImage rgb = toRgb(source);
    return compressJpeg(rgb, 0.72f);
  }

  static byte[] placeholderFrame(String label) throws IOException {
    BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = img.createGraphics();
    g.setColor(new Color(30, 30, 40));
    g.fillRect(0, 0, WIDTH, HEIGHT);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setColor(Color.LIGHT_GRAY);
    g.setFont(new Font("SansSerif", Font.PLAIN, 16));
    g.drawString(label == null ? "No camera" : label, 24, HEIGHT / 2);
    g.dispose();
    return compressJpeg(img, 0.75f);
  }

  private static BufferedImage toRgb(BufferedImage source) {
    if (source.getType() == BufferedImage.TYPE_INT_RGB) {
      return source;
    }
    BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g = rgb.createGraphics();
    g.drawImage(source, 0, 0, null);
    g.dispose();
    return rgb;
  }

  private static byte[] compressJpeg(BufferedImage rgb, float quality) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
    try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
      writer.setOutput(ios);
      ImageWriteParam param = writer.getDefaultWriteParam();
      if (param.canWriteCompressed()) {
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
      }
      writer.write(null, new IIOImage(rgb, null, null), param);
    } finally {
      writer.dispose();
    }
    return baos.toByteArray();
  }
}
