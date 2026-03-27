package com.auction.client.ui.BiddingForm;

import com.auction.client.app.NodeManager;
import com.auction.client.ui.Main.KhungController;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class BiddingFormController {
    @FXML private Pane RootPane;

    @FXML
    private void RemoveForm() {
        NodeManager.removeNodeFromPane(RootPane, KhungController.getKhungChua());
    }
}
