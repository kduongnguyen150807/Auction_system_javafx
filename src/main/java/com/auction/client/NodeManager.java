package com.auction.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class NodeManager {
    /* @param container: nơi chứa node
       @param fxmlPath: node cần add hoặc thay thế
     */
    public static void addNodeToPane(Pane container, String fxmlPath) throws IOException {
        // lấy node cần thêm
        Node node = FXMLLoader.load(NodeManager.class.getResource(fxmlPath));
        //thêm node
        container.getChildren().add(node);
    }
}
