package com.auction.client.ui.RatingForm;

import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.Rating;
import com.auction.shared.Request;
import com.auction.shared.Response;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class RatingFormController {
    @FXML private VBox RootPane;
    @FXML private Label TitleLabel;
    @FXML private HBox StarContainer;
    @FXML private TextArea FeedbackField;

    private int itemid;
    private int selectedstars = 0;
    private Label[] starlabels = new Label[5];
    private Runnable oncomplete;

    @FXML
    public void initialize() {
        for (int res = 0; res < 5; res++) {
            Label ans = new Label("\u2606");
            ans.setStyle("-fx-font-size: 32; -fx-text-fill: #e2b44d; -fx-cursor: hand;");
            int res1 = res + 1;
            ans.setOnMouseClicked(e -> selectstars(res1));
            starlabels[res] = ans;
            StarContainer.getChildren().add(ans);
        }
    }

    public void setdata(int itemid) {
        this.itemid = itemid;
    }

    public void setoncomplete(Runnable r) {
        this.oncomplete = r;
    }

    private void selectstars(int count) {
        this.selectedstars = count;
        for (int res = 0; res < 5; res++) {
            starlabels[res].setText(res < count ? "\u2605" : "\u2606");
        }
    }

    @FXML
    private void handlecancel() {
        NodeManager.removeNodeFromPane(RootPane, KhungController.getKhungChua());
    }

    @FXML
    private void handlesubmit() {
        if (selectedstars == 0) {
            showalert(Alert.AlertType.WARNING, "No Rating", "Please select at least 1 star.");
            return;
        }
        String res = FeedbackField.getText();
        Rating res1 = new Rating();
        res1.setitemid(this.itemid);
        res1.setstars(this.selectedstars);
        res1.setfeedback(res == null ? "" : res.trim());

        new Thread(() -> {
            Request res2 = new Request(Request.submitrating, res1);
            Response res3 = NetworkClient.getinstance().sendrequestandwait(res2);
            Platform.runLater(() -> {
                if (res3 != null && Response.ok.equals(res3.getstatus())) {
                    showalert(Alert.AlertType.INFORMATION, "Success", "Your rating has been submitted.");
                    NodeManager.removeNodeFromPane(RootPane, KhungController.getKhungChua());
                    if (oncomplete != null) oncomplete.run();
                } else {
                    String ans = res3 != null ? res3.getmessage() : "Failed";
                    showalert(Alert.AlertType.ERROR, "Error", ans);
                }
            });
        }).start();
    }

    private void showalert(Alert.AlertType type, String title, String content) {
        Alert res = new Alert(type);
        res.setTitle(title);
        res.setHeaderText(null);
        res.setContentText(content);
        res.showAndWait();
    }
}
