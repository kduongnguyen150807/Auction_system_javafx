package com.auction.client.network;

import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

/**
 * JavaFX prompts for server connection. Kept separate from socket I/O so transport stays testable.
 */
final class NetworkConnectionUi {

  Optional<String> promptForServerIp() {
    while (true) {
      TextInputDialog dialog = new TextInputDialog("127.0.0.1");
      dialog.setTitle("Server IP");
      dialog.setHeaderText("Enter Server IP address:");
      dialog.setContentText("IP (e.g. 127.0.0.1 or 192.168.1.x):");
      Optional<String> result = dialog.showAndWait();
      if (result.isEmpty()) return Optional.empty();
      String ip = result.get().trim();
      if (!ip.isBlank()) return Optional.of(ip);
      new Alert(Alert.AlertType.WARNING, "IP address cannot be empty.").showAndWait();
    }
  }

  void showConnectionError(String ip) {
    Runnable alert =
        () -> {
          Alert a = new Alert(Alert.AlertType.ERROR);
          a.setTitle("Connection Failed");
          a.setHeaderText("Cannot reach server at " + ip + ":8080");
          a.setContentText(
              "Make sure the server is running and the IP address is correct,\nthen restart the application.");
          a.showAndWait();
        };
    if (Platform.isFxApplicationThread()) alert.run();
    else Platform.runLater(alert);
  }
}
