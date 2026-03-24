package com.auction.client.ui.TrangChu;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class TrangChuController {
    @FXML
    private HBox TrendingBind;

    @FXML
    void initialize() {
        try{
            NodeContentLoader<HBox> Items = new NodeContentLoader<>();
            Items.load("/ui/itemTrangChu/Item.fxml");
            NodeManager.addNodeToPane(Items, TrendingBind);
        }catch (Exception e){
            System.out.println("not found");
        }

    }
}
