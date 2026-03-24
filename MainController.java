package com.example.giaodientest;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainController {
    @FXML
    public void openAddProductWindow(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("testmoi.fxml"));
            Parent root = fxmlLoader.load();
            Stage popupStage = new Stage();
            popupStage.setTitle("Thêm Sản Phẩm Mới");
            popupStage.setScene(new Scene(root));
            popupStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi không mở được cửa sổ: " + e.getMessage());
        }
    }
}