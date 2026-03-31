package com.auction.client.app;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class NodeContentLoader<T extends Node> {
  private T currentNode;
  private Object controller;

  public void load(String fxmlPath) throws IOException {
    URL location = getClass().getResource(fxmlPath);
    FXMLLoader loader = new FXMLLoader(location);
    currentNode = loader.load();
    controller = loader.getController();
  }

  public T getCurrentNode() {
    return currentNode;
  }

  @SuppressWarnings("unchecked")
  public <C> C getController() {
    return (C) controller;
  }
}
