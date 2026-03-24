package com.example.giaodientest;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("testtt.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 878, 766);
        stage.setTitle("AUCTION PRO - Hệ thống đấu giá trực tuyến");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}