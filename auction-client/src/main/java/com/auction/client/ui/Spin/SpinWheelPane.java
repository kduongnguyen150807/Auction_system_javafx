package com.auction.client.ui.Spin;

import java.util.List;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/** Visual spin wheel with 7 segments and a fixed pointer. */
public final class SpinWheelPane extends StackPane {

  private static final int SEGMENT_COUNT = 7;
  private static final double SEGMENT_ANGLE = 360.0 / SEGMENT_COUNT;
  private static final double WHEEL_SIZE = 340;
  private static final double RADIUS = WHEEL_SIZE / 2.0 - 6;
  private static final double LABEL_RADIUS = RADIUS * 0.58;

  private static final Color[] SEGMENT_FILLS = {
    Color.web("#3d3200"),
    Color.web("#2a2200"),
    Color.web("#2a1540"),
    Color.web("#1e1e1e"),
    Color.web("#0a2540"),
    Color.web("#0a3040"),
    Color.web("#401020")
  };

  private static final Color[] SEGMENT_STROKES = {
    Color.web("#ffd700"),
    Color.web("#ffaa00"),
    Color.web("#c084fc"),
    Color.web("#888888"),
    Color.web("#007aff"),
    Color.web("#00d4ff"),
    Color.web("#ff2a6d")
  };

  private final Group wheelGroup = new Group();
  private double currentRotation;

  public SpinWheelPane() {
    getStyleClass().add("spin-wheel-container");
    getChildren().add(wheelGroup);
    buildPointer();
  }

  public void setSegmentLabels(List<String> labels) {
    wheelGroup.getChildren().clear();
    currentRotation = 0;
    wheelGroup.setRotate(0);

    Circle backdrop = new Circle(0, 0, RADIUS + 4);
    backdrop.setFill(Color.web("#0d0d12"));
    backdrop.setStroke(Color.web("#ffd700", 0.55));
    backdrop.setStrokeWidth(3);
    wheelGroup.getChildren().add(backdrop);

    for (int i = 0; i < SEGMENT_COUNT; i++) {
      double start = i * SEGMENT_ANGLE - 90 - SEGMENT_ANGLE / 2;
      Arc arc = new Arc(0, 0, RADIUS, RADIUS, start, SEGMENT_ANGLE);
      arc.setType(ArcType.ROUND);
      arc.setFill(SEGMENT_FILLS[i % SEGMENT_FILLS.length]);
      arc.setStroke(SEGMENT_STROKES[i % SEGMENT_STROKES.length]);
      arc.setStrokeWidth(2.5);
      wheelGroup.getChildren().add(arc);
    }

    for (int i = 0; i < SEGMENT_COUNT; i++) {
      double boundaryDeg = i * SEGMENT_ANGLE - 90 - SEGMENT_ANGLE / 2;
      double rad = Math.toRadians(boundaryDeg);
      Line divider = new Line(0, 0, Math.cos(rad) * RADIUS, Math.sin(rad) * RADIUS);
      divider.setStroke(Color.web("#ffffff", 0.18));
      divider.setStrokeWidth(1.5);
      wheelGroup.getChildren().add(divider);
    }

    for (int i = 0; i < SEGMENT_COUNT; i++) {
      String label = labels != null && i < labels.size() ? labels.get(i) : ("#" + i);
      wheelGroup.getChildren().add(buildSegmentLabel(i, label));
    }

    Circle hub = new Circle(0, 0, 22);
    hub.setFill(Color.web("#111118"));
    hub.setStroke(Color.web("#ffd700", 0.75));
    hub.setStrokeWidth(2.5);
    wheelGroup.getChildren().add(hub);

    Circle hubInner = new Circle(0, 0, 8);
    hubInner.setFill(Color.web("#ffd700", 0.85));
    wheelGroup.getChildren().add(hubInner);
  }

  /**
   * Stops with segment {@code index} under the fixed top pointer.
   *
   * <p>Segment {@code i} bisector sits {@code i * SEGMENT_ANGLE}° clockwise from top at rest.
   * A clockwise wheel rotation {@code R} aligns that bisector with the pointer when
   * {@code (i * SEGMENT_ANGLE + R) ≡ 0 (mod 360)}, i.e. {@code R = 360 - i * SEGMENT_ANGLE}.
   */
  public void spinToSegment(int index, Runnable onFinished) {
    int safeIndex = Math.floorMod(index, SEGMENT_COUNT);
    double segmentFromTop = safeIndex * SEGMENT_ANGLE;
    double targetMod = positiveMod(360.0 - segmentFromTop, 360.0);
    double currentMod = positiveMod(currentRotation, 360.0);
    double delta = (360 * 6) + positiveMod(targetMod - currentMod, 360.0);
    double finalAngle = currentRotation + delta;

    RotateTransition spin = new RotateTransition(Duration.seconds(4.2), wheelGroup);
    spin.setFromAngle(currentRotation);
    spin.setToAngle(finalAngle);
    spin.setInterpolator(Interpolator.EASE_OUT);
    spin.setOnFinished(
        e -> {
          currentRotation = finalAngle;
          if (onFinished != null) {
            onFinished.run();
          }
        });
    spin.play();
  }

  private Group buildSegmentLabel(int index, String rawLabel) {
    double maxWidth = chordWidth(LABEL_RADIUS, SEGMENT_ANGLE) * 0.82;
    String display = formatSegmentLabel(rawLabel);

    Label label = new Label(display);
    label.setWrapText(true);
    label.setAlignment(Pos.CENTER);
    label.setTextAlignment(TextAlignment.CENTER);
    label.setMaxWidth(maxWidth);
    label.setPrefWidth(maxWidth);
    label.getStyleClass().add("wheel-segment-label");
    label.getStyleClass().add(styleClassForLabel(rawLabel));

    double bisectorFromTop = index * SEGMENT_ANGLE;
    if (bisectorFromTop > 90 && bisectorFromTop < 270) {
      label.setRotate(270);
    } else {
      label.setRotate(90);
    }

    label.setTranslateY(-LABEL_RADIUS);

    Group holder = new Group(label);
    holder.getTransforms().add(new javafx.scene.transform.Rotate(bisectorFromTop));
    return holder;
  }

  private static double chordWidth(double radius, double angleDeg) {
    return 2.0 * radius * Math.sin(Math.toRadians(angleDeg / 2.0));
  }

  private static String formatSegmentLabel(String raw) {
    if (raw == null || raw.isBlank()) {
      return "";
    }
    return switch (raw) {
      case "VIP 1 ngày" -> "VIP\n1 ngày";
      case "VIP 2 ngày" -> "VIP\n2 ngày";
      case "VIP 1 năm" -> "VIP\n1 năm";
      case "Chúc bạn may mắn lần sau" -> "May mắn\nlần sau";
      case "10$" -> "10$";
      case "100$" -> "100$";
      case "10000$" -> "10K$";
      default -> raw.length() > 12 ? raw.substring(0, 11) + "…" : raw;
    };
  }

  private static String styleClassForLabel(String raw) {
    if (raw == null) {
      return "wheel-label-default";
    }
    if (raw.contains("VIP")) {
      return "wheel-label-vip";
    }
    if (raw.contains("$")) {
      return raw.contains("10000") ? "wheel-label-jackpot" : "wheel-label-cash";
    }
    return "wheel-label-luck";
  }

  private static double positiveMod(double value, double modulus) {
    double result = value % modulus;
    return result < 0 ? result + modulus : result;
  }

  private void buildPointer() {
    Polygon pointer = new Polygon(0, -RADIUS - 22, -14, -RADIUS + 2, 14, -RADIUS + 2);
    pointer.getStyleClass().add("wheel-pointer");
    pointer.setFill(Color.web("#ffd700"));
    pointer.setStroke(Color.web("#ffffff", 0.9));
    pointer.setStrokeWidth(2);
    StackPane.setAlignment(pointer, Pos.TOP_CENTER);
    getChildren().add(pointer);
  }
}
