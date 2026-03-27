package com.auction.client.ui.ItemInformation;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.Main.KhungController;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class ItemInformationController {
    @FXML
    private void ShowBiddingForm() {
        try {
            NodeContentLoader<VBox> formLoader = new NodeContentLoader<>();
            formLoader.load("/fxml/biddingform/BiddingForm.fxml");
            NodeManager.addNodeToPane(formLoader, KhungController.getKhungChua());
        } catch (Exception ignored) {
        }
    }
}
