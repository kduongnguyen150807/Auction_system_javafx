package com.auction.client;
import javafx.application.Application;
import javafx.stage.Stage;
public class Main extends Application {
    @Override
    public void start(Stage primarystage) throws Exception {
        SceneManager.setstage(primarystage);
        primarystage.setTitle("hệ thống đấu giá online");
        SceneManager.switchscene("/fxml/welcome.fxml");
    }
    public static void main(String[] args) {
        launch(args);
    }
}