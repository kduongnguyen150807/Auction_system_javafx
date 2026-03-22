package com.auction.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ItemController {
    // khoi tao tham chieu den ndoe trong item
    @FXML
    private Label LabelName;

    @FXML
    private Label LabelPrice;

    @FXML
    private Label LabelQuantity;

    public void setData(String Name, String Price, String Quantity){
        // thay doi data phu hop voi du lieu
        LabelName.setText(Name);
        LabelPrice.setText(Price);
        LabelQuantity.setText(Quantity);
    }
}
