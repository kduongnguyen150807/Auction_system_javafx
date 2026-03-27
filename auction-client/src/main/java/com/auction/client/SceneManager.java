package com.auction.client;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
public class SceneManager {
    private static Stage s;
    private static final String GLOBAL_STYLE = "/css/GlobalColor.css";
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
        URL styleUrl = SceneManager.class.getResource(GLOBAL_STYLE);
        if (styleUrl != null) {
            String style = styleUrl.toExternalForm();
            if (!s.getScene().getStylesheets().contains(style)) {
                s.getScene().getStylesheets().add(style);
            }
        }
        s.show();
    }
}