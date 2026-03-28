package com.example.giaodientest;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class MainController {
    @FXML
    public void openAddProductWindow(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("testmoi.fxml"));
            Parent root = fxmlLoader.load();

            AddProductController addController = fxmlLoader.getController();
            addController.setMainController(this);

            Stage popupStage = new Stage();
            popupStage.setTitle("Thêm Sản Phẩm Mới");
            popupStage.setScene(new Scene(root));
            popupStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi không mở được cửa sổ: " + e.getMessage());
        }
    }
    @FXML
    public void openProfile(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Profile.fxml"));
            Parent root = fxmlLoader.load();

            // Lấy Stage (cửa sổ) hiện tại và thay thế Scene (giao diện) bên trong nó
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi không chuyển được sang trang cá nhân: " + e.getMessage());
        }
    }
    @FXML
    private FlowPane productContainer;
    public void addProductCard(Node card){
        productContainer.getChildren().add(card);
    }

}