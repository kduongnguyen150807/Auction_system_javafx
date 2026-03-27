package com.auction.client.ui.SuccessfullyRegister;

import com.auction.client.app.NodeManager;
import com.auction.client.ui.Main.KhungController;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

public class SuccessfullyRegisterController {
    @FXML
    private HBox RootPane;

    @FXML
    public void CloseAnnouncement(){
        NodeManager.removeNodeFromPane(RootPane, KhungController.getKhungChua());
    }
}
