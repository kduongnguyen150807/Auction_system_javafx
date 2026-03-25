package com.auction.client.app;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class NodeManager {


    /*
    @param loader: đối tượng chứa node đã load content
    @param backGroundFrame: back ground nền để add vào khung chính
     */

    public static void addNodeToPane(NodeContentLoader<?> loader, Pane backGroundFrame){
        Node contentNode = loader.getCurrentNode();

        backGroundFrame.getChildren().add(contentNode);
    }

    /*
    @param node1: node cần thay
    @param node2: node cần bị thay
     */

    public static void switchNodewithNode(Node node1, Node node2, Pane backGroundFrame){
        System.out.println("switching");
        var children = backGroundFrame.getChildren();
        System.out.println(children);
        System.out.println("got child");
        int index1 = children.indexOf(node2);
        System.out.println(index1);
        backGroundFrame.getChildren().set(index1, node1);
    }

    public static void removeNodeFromPane(Node node1, Pane backGroundFrame){
        backGroundFrame.getChildren().remove(node1);
    }
}
