package com.auction.client.ui.Main;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class KhungController {
    @FXML
    private HBox SearchContainer;

    @FXML
    private VBox ContentArea;

    @FXML
    public void initialize(){
        try{
            // load thanh tim kiem
            NodeContentLoader<HBox> ThanhTimKiemLoader = new NodeContentLoader<>();
            ThanhTimKiemLoader.load("/ui/SearchBar/ThanhTimKiem.fxml");

            // load TrangChu
            NodeContentLoader<ScrollPane> TrangChuLoader = new NodeContentLoader<>();
            TrangChuLoader.load("/ui/TrangChu/TrangChu.fxml");

            //Add node
            NodeManager.addNodeToPane(TrangChuLoader, ContentArea);
            NodeManager.addNodeToPane(ThanhTimKiemLoader, SearchContainer);
        } catch (IOException e) {

        }
    }
}
