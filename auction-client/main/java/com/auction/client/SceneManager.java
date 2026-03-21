package com.auction.client;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;
public class SceneManager {
    private static Stage s;
    public static void setStage(Stage stage) {
        s = stage;
    }
    public static void switchScene(String fxmlFile) throws IOException {
        Parent res = FXMLLoader.load(Objects.requireNonNull(SceneManager.class.getResource(fxmlFile)));
        Scene ans = new Scene(res, 800, 600);
        s.setScene(ans);
        s.show();
    }
}