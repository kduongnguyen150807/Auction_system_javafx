package com.auction.client.app;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class NodeManager {
    public static void addNodeToPane(NodeContentLoader<?> loader, Pane backGroundFrame) {
        Node contentNode = loader.getCurrentNode();
        backGroundFrame.getChildren().add(contentNode);
    }

    public static void switchNodewithNode(Node node1, Node node2, Pane backGroundFrame) {
        int index1 = backGroundFrame.getChildren().indexOf(node2);
        if (index1 >= 0) {
            backGroundFrame.getChildren().set(index1, node1);
        }
    }

    public static void removeNodeFromPane(Node node1, Pane backGroundFrame) {
        backGroundFrame.getChildren().remove(node1);
    }
}
