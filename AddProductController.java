package com.example.giaodientest;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class AddProductController {

    @FXML
    private ComboBox<String> classifyComboBox;

    @FXML
    private ImageView productImageView;

    @FXML private TextField txtName;
    @FXML private TextField txtPrice;
    @FXML private TextField txtQuantity;

    private MainController mainController;
    private String selectedImagePath = null;

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
            // Chuyển đường dẫn file thành chuẩn URI của JavaFX để lưu lại
            selectedImagePath = selectedFile.toURI().toString();
            System.out.println("Đã chọn ảnh tại: " + selectedImagePath);

            try {
                Image newProductImage = new Image(selectedImagePath);
                productImageView.setImage(newProductImage);

            } catch (Exception e) {
                System.out.println("Lỗi hiển thị ảnh: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Bạn chưa chọn ảnh nào.");
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    @FXML
    public void handleUpload(ActionEvent event) {
        try {
            String name = txtName.getText();
            String price = txtPrice.getText();
            String quantity = txtQuantity.getText();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProductCard.fxml"));
            Node newCard = loader.load();

            ProductCardController cardController = loader.getController();

            cardController.setData(name, price, quantity, selectedImagePath);

            if (mainController != null) {
                mainController.addProductCard(newCard);
            }

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi không tải được khuôn đúc ProductCard.fxml!");
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("Lỗi Null! Hãy chắc chắn bạn đã đặt đúng fx:id (txtName, txtPrice, txtQuantity) và nút Upload đã được gán sự kiện!");
        }
    }
}