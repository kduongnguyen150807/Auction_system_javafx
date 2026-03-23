package com.auction.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/Khung.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 1024);
        stage.setTitle("Pro Auction!");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();

        KhungController khungController = fxmlLoader.getController();
        khungController.initializer();
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}
