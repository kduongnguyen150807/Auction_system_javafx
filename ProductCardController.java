package com.example.giaodientest;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ProductCardController {
    @FXML
    private VBox rootPane;
    @FXML
    private ImageView imgProduct;
    @FXML
    private Label lblName;
    @FXML
    private Label lblPrice;
    @FXML
    private Label lblQuantity;
    public void setData(String name, String price, String quantity, String imagePath) {
        lblName.setText(name);
        lblPrice.setText(price);
        lblQuantity.setText(quantity);

        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image productImg = new Image(imagePath);
                imgProduct.setImage(productImg);

                imgProduct.setFitWidth(200);
                imgProduct.setFitHeight(180);
                imgProduct.setPreserveRatio(true);
                imgProduct.setSmooth(true);
                imgProduct.setCache(true);

            } catch (Exception e) {
                System.out.println("Lỗi load ảnh trong ô sản phẩm: " + e.getMessage());
            }
        } else {
        }
    }
}