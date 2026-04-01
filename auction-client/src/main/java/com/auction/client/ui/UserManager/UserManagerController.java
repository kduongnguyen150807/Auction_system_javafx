package com.auction.client.ui.UserManager;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.UserCard.UserCardController;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public class UserManagerController {
    @FXML
    private VBox UserBox;

    @FXML
    private Button currentPage;

    private List<NodeContentLoader> userCardList = new ArrayList<>();

    private static Integer currentInt = 1;
    private final int ITEMS_PER_PAGE = 4;


    @FXML
    void initialize(){
        try{
            for(int i = 0; i<= 10; i++){
                NodeContentLoader<HBox> userCard = new NodeContentLoader<>();
                userCard.load("/fxml/usercard/UserCard.fxml");
                UserCardController userCardController = (UserCardController) userCard.getController();
                userCardController.setData("client " + i);
                System.out.println("adding");
                userCardList.add(userCard);
            }

            NodeManager.addNodeToPane(userCardList.get(0), UserBox);
            NodeManager.addNodeToPane(userCardList.get(1), UserBox);
            NodeManager.addNodeToPane(userCardList.get(2), UserBox);
            NodeManager.addNodeToPane(userCardList.get(3), UserBox);
        } catch (Exception e) {
            System.out.println("user card not found");
        }
    }

    @FXML
    void NextPage() {
        int maxPage = (int) Math.ceil((double) userCardList.size() / ITEMS_PER_PAGE);
        if (currentInt < maxPage) {
            currentInt++;
            updateDisplay();
        }
    }

    @FXML
    void PrevPage() {
        if (currentInt > 1) {
            currentInt--;
            updateDisplay();
        }
    }

    private void updateDisplay() {
        UserBox.getChildren().clear();
        currentPage.setText(currentInt.toString());
        int startIndex = (currentInt - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, userCardList.size());

        // 3. Thêm các card mới vào
        for (int i = startIndex; i < endIndex; i++) {
            NodeManager.addNodeToPane(userCardList.get(i), UserBox);
        }
    }
}