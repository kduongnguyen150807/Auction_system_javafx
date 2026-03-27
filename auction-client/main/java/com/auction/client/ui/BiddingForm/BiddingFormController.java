package com.auction.client.ui.BiddingForm;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.Main.KhungController;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class BiddingFormController {
    @FXML
    private Pane RootPane;

    @FXML
    private void RemoveForm() throws IOException {
        NodeManager.removeNodeFromPane(RootPane, KhungController.getKhungChua());

        NodeContentLoader<HBox> NewAnnouncement = new NodeContentLoader<>();
        NewAnnouncement.load("/ui/SuccessfullyRegister/SuccessfullyRegister.fxml");

        NodeManager.addNodeToPane(NewAnnouncement.getCurrentNode(), KhungController.getKhungChua());
    }
}
