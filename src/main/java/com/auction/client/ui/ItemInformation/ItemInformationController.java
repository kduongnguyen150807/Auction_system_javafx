package com.auction.client.ui.ItemInformation;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.Main.KhungController;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class ItemInformationController {
    @FXML
    private void ShowBiddingForm(){
        try{
            NodeContentLoader<VBox> BiddingFormLoader = new NodeContentLoader<>();
            BiddingFormLoader.load("/ui/BiddingForm/BiddingForm.fxml");

            NodeManager.addNodeToPane(BiddingFormLoader.getCurrentNode(), KhungController.getKhungChua());
        }catch (Exception e){
            System.out.println("null form");
        }

    }
}
