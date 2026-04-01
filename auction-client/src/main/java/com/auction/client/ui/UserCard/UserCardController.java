package com.auction.client.ui.UserCard;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class UserCardController {
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    @FXML
    private HBox userRow;

    @FXML
    private Label name;

    @FXML
    public void initialize() {
        userRow.setOnMouseClicked(event -> {
            boolean isAlreadySelected = userRow.getPseudoClassStates().contains(SELECTED_PSEUDO_CLASS);

            if (isAlreadySelected) {
                userRow.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
            } else {
                userRow.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, true);
                userRow.requestFocus();
            }
        });
    }

    public void setData(String name){
        this.name.setText(name);
    }
}