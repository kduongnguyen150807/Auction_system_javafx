package com.auction.client.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

public class NodeLoader {
  private final FXMLLoader loader;

  public NodeLoader(String fxmlPath) throws IOException {
    loader = new FXMLLoader(getClass().getResource(fxmlPath));
  }

  public Node getCurrentNode() throws IOException {
    return loader.load();
  }

  public <T> T getController() {
    return loader.getController();
  }
}
