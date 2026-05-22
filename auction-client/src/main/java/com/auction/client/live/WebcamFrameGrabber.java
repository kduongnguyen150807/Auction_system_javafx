package com.auction.client.live;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.image.BufferedImage;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Opens the default webcam and returns frames (non-FX thread only). */
final class WebcamFrameGrabber implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(WebcamFrameGrabber.class);

  private Webcam webcam;

  boolean open() {
    System.setProperty("java.awt.headless", "false");
    webcam = pickWebcam();
    if (webcam == null) {
      logger.warn("No webcam device found");
      return false;
    }
    try {
      webcam.setViewSize(WebcamResolution.QVGA.getSize());
      webcam.open(true);
      if (!webcam.isOpen()) {
        logger.warn("Webcam failed to open: {}", webcam.getName());
        webcam = null;
        return false;
      }
      logger.info("Webcam ready: {}", webcam.getName());
      return true;
    } catch (Exception e) {
      logger.warn("Webcam open failed: {}", e.getMessage());
      close();
      return false;
    }
  }

  BufferedImage grabFrame() {
    if (webcam == null || !webcam.isOpen()) {
      return null;
    }
    return webcam.getImage();
  }

  @Override
  public void close() {
    if (webcam != null && webcam.isOpen()) {
      try {
        webcam.close();
      } catch (Exception ignored) {
      }
    }
    webcam = null;
  }

  private static Webcam pickWebcam() {
    Webcam def = Webcam.getDefault();
    if (def != null) {
      return def;
    }
    List<Webcam> all = Webcam.getWebcams();
    return all.isEmpty() ? null : all.getFirst();
  }
}
