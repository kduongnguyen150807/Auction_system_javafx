package com.auction.client.ui.ItemInformation;
import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.ui.Main.KhungController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
public class ItemInformationController {
    @FXML private ImageView ItemImageHolder;
    @FXML private Label ItemName;
    @FXML private Label ItemDescription;
    @FXML private Label CurrentHighestBidValue;
    @FXML private Label EndsInValue;
    private int itemid = -1;
    private String itemname = "";
    public void setData(int itemid, String name, double currenthighestbid, String description, String endsin, String imageurl) {
        this.itemid = itemid;
        this.itemname = name == null ? "" : name;
        if (ItemName != null) ItemName.setText(this.itemname);
        if (ItemDescription != null) ItemDescription.setText(description == null ? "" : description);
        if (CurrentHighestBidValue != null) CurrentHighestBidValue.setText(String.format("%,.2f$", currenthighestbid));
        if (EndsInValue != null) EndsInValue.setText(endsin == null ? "" : endsin);
        if (ItemImageHolder != null && imageurl != null && !imageurl.isBlank()) {
            String ans = imageurl.contains(".webp") ? imageurl.replace(".webp", ".jpg") : imageurl;
            ItemImageHolder.setImage(new Image(ans, true));
        }
    }
    @FXML
    private void ShowBiddingForm() {
        try {
            NodeContentLoader<VBox> res = new NodeContentLoader<>();
            res.load("/fxml/biddingform/BiddingForm.fxml");
            Object ans = res.getController();
            if (ans instanceof com.auction.client.ui.BiddingForm.BiddingFormController) {
                com.auction.client.ui.BiddingForm.BiddingFormController res2 = (com.auction.client.ui.BiddingForm.BiddingFormController) ans;
                if (itemid > 0) res2.setData(itemid, itemname);
                res2.setbidvaluelabel(CurrentHighestBidValue);
            }
            NodeManager.addNodeToPane(res, KhungController.getKhungChua());
        } catch (Exception ignored) {
        }
    }
}
