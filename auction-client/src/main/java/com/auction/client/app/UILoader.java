package com.auction.client.app;

import com.auction.client.ui.base.CanCloseWindow;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class UILoader {
  private UILoader() {

  }

  public static Stage ModalStage(Parent node, Window owner) {
    Stage stage = new Stage();
    Scene scene = new Scene(node);
    scene.setFill(Color.TRANSPARENT);

    stage.setScene(scene);
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.initOwner(owner);
    stage.setResizable(false);
    stage.initStyle(StageStyle.TRANSPARENT);


    if (node instanceof CanCloseWindow ccw) {
      ccw.setCloseWindow(stage::close);
    }

    return stage;
  }

  public static void showModal(Parent node, Window parentWindow) {
    Stage stage = ModalStage(node, parentWindow);
    stage.showAndWait();
  }
}
