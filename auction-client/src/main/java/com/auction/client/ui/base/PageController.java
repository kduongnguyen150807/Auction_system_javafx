package com.auction.client.ui.base;

import java.util.function.Consumer;

public abstract class PageController<T> {
  protected Consumer<T> switchView;

  public void setSwitchView(Consumer<T> switchView) {
    this.switchView = switchView;
  }
}
