package com.auction.client.ui.ChatLabel;

import javafx.fxml.FXML;

import javafx.scene.control.Label;

public class ChatLabelController {
    @FXML private Label username;
    @FXML private Label message;

    public void setData(String a, String b){
        username.setText(a);
        message.setText(b);
    }
}
