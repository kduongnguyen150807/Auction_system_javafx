package com.auction.client;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class KhungController {
    @FXML
    private VBox SearchContainer;

    public void initializer(){
        try{
            // them thanh tim kiem
            NodeManager.addNodeToPane(SearchContainer, "/fxml/ThanhTimKiem.fxml");
            System.out.println("found");
        } catch (IOException e) {
            System.out.println("not found");
        }
    }
}
