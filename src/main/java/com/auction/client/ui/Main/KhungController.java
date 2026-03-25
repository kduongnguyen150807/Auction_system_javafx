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

public class KhungController {

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
            TrangChuLoader.load("/ui/TrangChu/TrangChu.fxml");

            //Add node
            NodeManager.addNodeToPane(TrangChuLoader, ContentArea);
            NodeManager.addNodeToPane(ThanhTimKiemLoader, SearchContainer);

            currentNode = TrangChuLoader.getCurrentNode();
        } catch (IOException e) {
            System.out.println("null url");
        }
    }

    public static Pane getKhungChua() {
        return KhungChua;
    }

    public static Node getCurrentNode() {
        return currentNode;
    }
}
