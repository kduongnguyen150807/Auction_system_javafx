package com.auction.client.util;

import com.auction.client.ui.base.CanCloseWindow;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class StageUtil {
  public static Stage construcModalStage(Parent node, Window owner) {
    Stage stage = new Stage();
    Scene scene = new Scene(node);
    stage.setScene(scene);

    scene.setFill(Color.TRANSPARENT);

    stage.initModality(Modality.APPLICATION_MODAL);
    stage.initStyle(StageStyle.TRANSPARENT);
    stage.setResizable(false);
    stage.initOwner(owner);

    if (node instanceof CanCloseWindow ccw) {
      ccw.setCloseWindow(stage::close);
    }

    return stage;
  }

  public static void showModalStage(Parent node, Window owner) {
    construcModalStage(node, owner).showAndWait();
  }
}
