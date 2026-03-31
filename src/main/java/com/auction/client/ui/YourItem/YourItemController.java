package com.auction.client.ui.YourItem;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.ItemCard.ItemCardController;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

public class YourItemController {
    @FXML
    private FlowPane ItemContainer;

    @FXML
    void initialize(){
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
                    controller.setData(name, price, desc, time);
                }else{
                }

                NodeManager.addNodeToPane(Items.getCurrentNode(), ItemContainer);
            }
            System.out.println("set up inventory completed");
        }catch (Exception e){
            System.out.println("exception");
        }
    }

}
