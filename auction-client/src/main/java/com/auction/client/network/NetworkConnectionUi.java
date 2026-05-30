package com.auction.client.network;

import javafx.application.Platform;
import javafx.scene.control.Alert;

/** JavaFX alerts for server connection failures. */
final class NetworkConnectionUi {

  void showConnectionError(String ip) {
    Runnable alert =
        () -> {
          Alert a = new Alert(Alert.AlertType.ERROR);
          a.setTitle("Connection Failed");
          a.setHeaderText("Cannot reach server at " + ip + ":" + NetworkClient.SERVER_PORT);
          a.setContentText(
              "Make sure the server is running and the IP address is correct,\nthen restart the application.");
          a.showAndWait();
        };
    if (Platform.isFxApplicationThread()) alert.run();
    else Platform.runLater(alert);
  }
}
