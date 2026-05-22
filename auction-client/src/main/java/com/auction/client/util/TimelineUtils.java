package com.auction.client.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;

public class TimelineUtils {
  public static Timeline setTimeline(Timeline timeline, int duration, List<Runnable> runnables) {
    if (runnables == null || runnables.isEmpty()) {
      return null;
    }

    timeline = new Timeline(
      new KeyFrame(
        Duration.seconds(duration),
        event -> {
          for (Runnable runnable : runnables) {
            if (runnable != null) {
              runnable.run();
            }

          }
        }
      )
    );
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();

    return timeline;
  }
}
