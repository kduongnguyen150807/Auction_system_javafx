package com.auction.client.util;

import javafx.application.Platform;

public class FXThread {
  private FXThread() {}

  public static void run(Runnable runnable) {
    if (Platform.isFxApplicationThread()) {
      runnable.run();
    } else {
      Platform.runLater(runnable);
    }
  }
}
