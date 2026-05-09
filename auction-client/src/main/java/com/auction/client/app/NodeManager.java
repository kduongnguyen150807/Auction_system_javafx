package com.auction.client.app;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class NodeManager {
  public static void addNodeToPane(NodeContentLoader<?> loader, Pane backGroundFrame) {
    Node contentNode = loader.getCurrentNode();
    backGroundFrame.getChildren().add(contentNode);
  }

  public static void switchNodewithNode(Node replacement, Node existing, Pane backGroundFrame) {
    int index = backGroundFrame.getChildren().indexOf(existing);
    if (index >= 0) {
      backGroundFrame.getChildren().set(index, replacement);
    }
  }

  public static void removeNodeFromPane(Node nodeToRemove, Pane backGroundFrame) {
    backGroundFrame.getChildren().remove(nodeToRemove);
  }
}
