package com.auction.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class TrangChuController {
    @FXML
    private HBox OngoingBind;
    //khởi tạo vbox chứa thông tin vật phẩm đấu giá
    public void HienThiVatPhamDauGia() throws IOException {
        for(int i = 0; i <= 11; i++){
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Item.fxml")
            );

            VBox item = loader.load();

            ItemController newController = loader.getController();

            newController.setData("Item " + i, i*100 + "$", i + "");
            OngoingBind.getChildren().add(item);
        }
    }
}
