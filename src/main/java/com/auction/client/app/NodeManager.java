package com.auction.client.app;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class NodeManager {

    private Pane mainFrame;
    /*
    @param loader: đối tượng chứa node đã load content
    @param backGroundFrame: back ground nền để add vào khung chính
     */

    public static void addNodeToPane(NodeContentLoader<?> loader, Pane backGroundFrame){
        Node contentNode = loader.getCurrentNode();

        backGroundFrame.getChildren().add(contentNode);
    }


}
