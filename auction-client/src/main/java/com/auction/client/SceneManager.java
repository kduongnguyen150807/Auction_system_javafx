package com.auction.client;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;
public class SceneManager {
    private static Stage s;
    public static void setstage(Stage stg) {
        s = stg;
    }
    public static Stage getstage() {
        Stage ans = s;
        return ans;
    }
    public static void switchscene(String fxmlfile) throws IOException {
        Parent res = FXMLLoader.load(Objects.requireNonNull(SceneManager.class.getResource(fxmlfile)));
        Scene currentScene = s.getScene();
        if (currentScene == null) {
            s.setScene(new Scene(res));
        } else {
            currentScene.setRoot(res);
        }
        s.show();
    }
}