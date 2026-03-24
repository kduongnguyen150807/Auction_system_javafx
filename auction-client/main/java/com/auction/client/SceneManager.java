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
        Scene ans = new Scene(res, 800, 600);
        s.setScene(ans);
        s.show();
    }
}