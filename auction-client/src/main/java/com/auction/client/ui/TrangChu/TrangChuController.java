package com.auction.client.ui.TrangChu;

import com.auction.client.app.NodeContentLoader;
import com.auction.client.app.NodeManager;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.ItemCard.ItemCardController;
import com.auction.shared.Item;
import com.auction.shared.Request;
import com.auction.shared.Response;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class TrangChuController {
    @FXML private HBox TrendingBind;

    @FXML
    void initialize() {
        refreshItems();
    }

    public void refreshItems() {
        Thread worker = new Thread(() -> {
            try {
                NetworkClient.getinstance().sendrequest(new Request(Request.list, null));
                Response res = NetworkClient.getinstance().receiveresponse();
                if (res == null || !Response.ok.equals(res.getstatus())) return;
                Object payload = res.getpayload();
                if (!(payload instanceof List<?> rawItems)) return;
                Platform.runLater(() -> renderItems(rawItems));
            } catch (Exception ignored) {
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    private void renderItems(List<?> rawItems) {
        TrendingBind.getChildren().clear();
        for (Object obj : rawItems) {
            if (!(obj instanceof Item item)) continue;
            try {
                NodeContentLoader<HBox> loader = new NodeContentLoader<>();
                loader.load("/fxml/itemcard/ItemCard.fxml");
                ItemCardController controller = loader.getController();
                if (controller != null) {
                    controller.setData(
                            safe(item.getname()),
                            item.getcurrentprice(),
                            safe(item.getdescription()),
                            formatTimeRemaining(item.getendtime())
                    );
                }
                NodeManager.addNodeToPane(loader, TrendingBind);
            } catch (Exception ignored) {
            }
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String formatTimeRemaining(LocalDateTime endTime) {
        if (endTime == null) return "N/A";
        Duration d = Duration.between(LocalDateTime.now(), endTime);
        if (d.isNegative() || d.isZero()) return "Da ket thuc";
        long totalHours = d.toHours();
        long days = totalHours / 24;
        long hours = totalHours % 24;
        if (days > 0) return days + "d " + hours + "h";
        long minutes = d.toMinutes() % 60;
        return hours + "h " + minutes + "m";
    }
}
