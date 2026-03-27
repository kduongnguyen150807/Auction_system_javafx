package com.auction.client.ui.Main;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class
KhungController {

    private static Pane KhungChua;
    private static Node currentNode;

    @FXML
    private HBox SearchContainer;

    @FXML
    private StackPane ContentArea;

    @FXML
    public void initialize(){
        KhungChua = ContentArea;

        try{
            // load thanh tim kiem
            NodeContentLoader<HBox> ThanhTimKiemLoader = new NodeContentLoader<>();
            ThanhTimKiemLoader.load("/ui/SearchBar/ThanhTimKiem.fxml");
            System.out.println("found searchbar");

            // load TrangChu
            NodeContentLoader<ScrollPane> TrangChuLoader = new NodeContentLoader<>();
            TrangChuLoader.load("/ui/Profile/Profile.fxml");

            //Add node
            NodeManager.addNodeToPane(TrangChuLoader.getCurrentNode(), ContentArea);
            NodeManager.addNodeToPane(ThanhTimKiemLoader.getCurrentNode(), SearchContainer);

            currentNode = TrangChuLoader.getCurrentNode();
        } catch (IOException e) {
            System.out.println("null url");
        }
    }

    @FXML
    public void OpenTrangChu() throws IOException{
        NodeContentLoader<ScrollPane> TrangChuLoader = new NodeContentLoader<>();
        TrangChuLoader.load("/ui/TrangChu/TrangChu.fxml");

        Node[] NodeListInContentArea = ContentArea.getChildren().toArray(new Node[0]);
        for(Node n: NodeListInContentArea){
            ContentArea.getChildren().remove(n);
        }

        currentNode = TrangChuLoader.getCurrentNode();
        NodeManager.addNodeToPane(TrangChuLoader.getCurrentNode(), ContentArea);
    }

    @FXML
    public void OpenProfile() throws IOException{
        NodeContentLoader<ScrollPane> ProfileLoader = new NodeContentLoader<>();
        ProfileLoader.load("/ui/Profile/Profile.fxml");

        Node[] NodeListInContentArea = ContentArea.getChildren().toArray(new Node[0]);
        for(Node n: NodeListInContentArea){
            ContentArea.getChildren().remove(n);
        }

        currentNode = ProfileLoader.getCurrentNode();
        NodeManager.addNodeToPane(ProfileLoader.getCurrentNode(), ContentArea);
    }

    public static Pane getKhungChua() {
        return KhungChua;
    }

    public static Node GetCurrentNode() {
        return currentNode;
    }

    public static void switchNodeInContentArea(Node node1, Node node2){
        NodeManager.switchNodewithNode(node1, node2, KhungChua);

        currentNode = node1;
    }
}
