package com.auction.client.ui.TrangChu;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.ItemCard.ItemCardController;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

public class TrangChuController {
    @FXML
    private HBox TrendingBind;

    @FXML
    void initialize() {
        try{
            for(int i = 1; i<= 10; i++){
                NodeContentLoader<HBox> Items = new NodeContentLoader<>();
                Items.load("/ui/ItemCard/ItemCard.fxml");

                ItemCardController controller = (ItemCardController) Items.getController();
                if (controller != null) {
                    String name = "Sản phẩm #" + i;
                    double price = 100.0 * i;
                    String desc = "Mô tả chi tiết cho sản phẩm thứ " + i;
                    String time = i + " ngày";
                    System.out.println("not null");
                    controller.setData(name, price, desc, time);
                }else{
                }

                NodeManager.addNodeToPane(Items, TrendingBind);
            }

        }catch (Exception e){
            System.out.println("exception");
        }

    }
}
