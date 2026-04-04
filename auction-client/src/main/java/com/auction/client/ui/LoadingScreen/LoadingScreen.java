package com.auction.client.ui.LoadingScreen;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class LoadingScreen {
    public static void showLoading(Pane rootPane) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(LoadingScreen.class.getResource("/fxml/loadingscreen/loadingscreen.fxml"));
                Node loadingNode = loader.load();
                loadingNode.setId("globalLoadingScreen");
                rootPane.getChildren().add(loadingNode);

            } catch (Exception e) {
                System.out.println("Lỗi không tìm thấy file Loading FXML!");
                e.printStackTrace();
            }
        });
    }
    public static void hideLoading(Pane rootPane) {
        Platform.runLater(() -> {
            rootPane.getChildren().removeIf(node -> "globalLoadingScreen".equals(node.getId()));
        });
    }
}