package com.auction.client.ui.utils;

import javafx.application.Platform;

import java.util.function.Consumer;

public class FXThread {
  public static Void dispatch(Runnable runnable) {
    if (Platform.isFxApplicationThread()) {
      runnable.run();
    } else {
      Platform.runLater(runnable);
    }
    return null;
  }

  public static <T> Void dispatch(Consumer<T> callback, T object) {
    if (Platform.isFxApplicationThread()) {
      callback.accept(object);
    } else {
      Platform.runLater(() -> callback.accept(object));
    }
    return null;
  }
}