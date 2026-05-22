package com.auction.client.ui.homeview.controller.profile;

import com.auction.client.app.AutoInject;
import com.auction.client.service.user.AuthService;
import com.auction.client.service.user.ClientService;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.ui.component.UserCard;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.FXThread;
import com.auction.shared.Response;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class ProfileController{
  private static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

  private ClientSession currentSession;

  @FXML private IdentityLayoutController identityLayoutController;
  @FXML private WalletLayoutController walletLayoutController;
  @FXML private MetricLayoutController metricLayoutController;
  @FXML private UserCard userCard;

  private final ClientService clientService;
  private final AuthService authService;

  @AutoInject
  public ProfileController(ClientService clientService, AuthService authService) {
    this.clientService = clientService;
    this.authService = authService;
  }

  @FXML
  private void initialize() {
    currentSession = ClientSession.CURRENT_SESSION.getCurrentSession();
    currentSession.currentUserProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        applySession(currentSession);
      }
    });
    applySession(currentSession);

    clientService.refreshUserTransaction()
      .exceptionally(ex -> {
        LOGGER.error("Không thể tự động cập nhật lịch sử giao dịch khi khởi tạo trang cá nhân", ex);
        return null;
      });
  }

  private void applySession(ClientSession session) {
    if (identityLayoutController != null) identityLayoutController.setClientSession(session);
    if (walletLayoutController != null) walletLayoutController.setClientSession(session);
    if (metricLayoutController != null) metricLayoutController.setClientSession(session);
    if (userCard != null) userCard.setClientSession(session);
  }

  public void handleLogout(ActionEvent actionEvent) {
    authService.signOut();
  }

  public void handleChangeAvatar(ActionEvent actionEvent) {
    FileChooser fc = new FileChooser();
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
    File file = fc.showOpenDialog(null);

    if (file == null) {
      return;
    }

    CompletableFuture.supplyAsync(() -> {
        try {
          return java.nio.file.Files.readAllBytes(file.toPath());
        } catch (Exception e) {
          throw new RuntimeException("Không thể đọc tệp tin ảnh đã chọn", e);
        }
      })
      .thenCompose(bytes -> {
        return clientService.uploadImage("https://api.cloudinary.com/v1_1/khanhdn-tk/image/upload", bytes);
      })
      .thenAccept(response -> FXThread.run(() -> {
        if (response != null && Response.OK.equals(response.getStatus())) {
          AlertUtil.showInfoAlert("Update Avatar", "Cập nhật ảnh đại diện thành công!");
        } else {
          String errorMsg = (response != null) ? response.getMessage() : "Máy chủ từ chối cập nhật ảnh đại diện.";
          AlertUtil.showErrorAlert("Update Avatar Failed", errorMsg);
        }
      }))
      .exceptionally(ex -> {
        FXThread.run(() -> {
          LOGGER.error("Lỗi xảy ra trong chuỗi tiến trình cập nhật ảnh đại diện", ex);
          AlertUtil.showErrorAlert("Update Avatar Error", "Quá trình tải ảnh lên thất bại. Vui lòng thử lại!");
        });
        return null;
      });
  }
}