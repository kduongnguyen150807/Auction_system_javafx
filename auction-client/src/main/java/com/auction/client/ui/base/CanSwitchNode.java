package com.auction.client.ui.base;

import java.util.function.Consumer;

public interface CanSwitchNode<T> {
  void setSwitchNode(Consumer<T> switchNode);
}
