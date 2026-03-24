package com.example.giaodientest;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class AddProductController {
    @FXML
    private ComboBox<String> classifyComboBox;
    @FXML
    private ImageView productImageView;
    @FXML
    public void initialize() {
        classifyComboBox.getItems().addAll("Art", "Car", "Watches", "Fashion", "Electronics");

    }
    @FXML
    public void handleChoosePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm mới");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        Stage stage = (Stage) classifyComboBox.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            System.out.println("Đã chọn ảnh tại: " + selectedFile.getAbsolutePath());

            try {
                Image newProductImage = new Image("file:" + selectedFile.getAbsolutePath());
                productImageView.setImage(newProductImage);

            } catch (Exception e) {
                System.out.println("Lỗi hiển thị ảnh: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Bạn chưa chọn ảnh nào.");
        }
    }
}