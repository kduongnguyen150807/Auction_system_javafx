package com.auction.client.ui.homeview.controller.profile;

import com.auction.client.network.NetworkEventListener;
import com.auction.client.service.AuthService;
import com.auction.client.service.UserService;
import com.auction.client.store.ClientSession;
import com.auction.client.ui.component.UserCard;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.ImagePresentationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ProfileController implements NetworkEventListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

  private ClientSession currentSession;

  @FXML private IdentityLayoutController identityLayoutController;
  @FXML private WalletLayoutController walletLayoutController;
  @FXML private MetricLayoutController metricLayoutController;

  @FXML private UserCard userCard;

  @FXML
  private void initialize() {
    currentSession = ClientSession.CURRENT_SESSION.getCurrentSession();
    currentSession.currentUserProperty().addListener((observable, oldValue, newValue) -> {
      apply(currentSession);
    });
    apply(currentSession);
  }

  private void apply(ClientSession session) {
    identityLayoutController.setClientSession(session);
    walletLayoutController.setClientSession(session);
    metricLayoutController.setClientSession(session);
    userCard.setClientSession(session);
  }

  public void handleLogout(ActionEvent actionEvent) {
    AuthService.getInstance().signOut();
  }

  public void handleChangeAvatar(ActionEvent actionEvent) throws IOException {
    javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
    java.io.File file = fc.showOpenDialog(null);
    if (file != null) {
      byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
      String message = UserService.uploadImage("https://api.cloudinary.com/v1_1/khanhdn-tk/image/upload", bytes);
      if (message != null) {
        AlertUtil.showErrorAlert("Update Avatar", message);
      }
    }
  }
}
