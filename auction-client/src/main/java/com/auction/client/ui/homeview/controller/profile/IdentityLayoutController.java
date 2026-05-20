package com.auction.client.ui.homeview.controller.profile;

import com.auction.client.app.AutoInject;
import com.auction.client.service.user.ClientService;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.FXThread; // Đảm bảo dùng helper bọc luồng UI an toàn
import com.auction.shared.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class IdentityLayoutController {
  @FXML private TextField fullNameInput;
  @FXML private Label fullNameLabel;
  @FXML private Label phoneLabel;
  @FXML private TextField phoneInput;
  @FXML private Label emailLabel;
  @FXML private TextField emailInput;
  @FXML private Button editButton;

  private boolean isEditing = false;
  private ClientSession clientSession;
  private ClientService clientService;

  @AutoInject
  public IdentityLayoutController(ClientService clientService) {
    this.clientService = clientService;
  }

  public void setClientSession(ClientSession session) {
    unbind();
    this.clientSession = session;
    bind();
  }

  private void unbind() {
    isEditing = false;
    setVisible(isEditing);

    if (fullNameLabel != null) fullNameLabel.textProperty().unbind();
    if (phoneLabel != null) phoneLabel.textProperty().unbind();
    if (emailLabel != null) emailLabel.textProperty().unbind();
  }

  private void bind() {
    if (clientSession != null) {
      if (fullNameLabel != null) fullNameLabel.textProperty().bind(clientSession.currentNameProperty());
      if (phoneLabel != null) phoneLabel.textProperty().bind(clientSession.phoneNumberProperty());
      if (emailLabel != null) emailLabel.textProperty().bind(clientSession.emailProperty());
    }
  }

  @FXML
  private void handleEditProfile() {
    if (isEditing) {
      saveChange();
    } else {
      isEditing = true;
      if (clientSession != null && clientSession.getCurrentUser() != null) {
        fullNameInput.setText(clientSession.getCurrentUser().getFullName());
        phoneInput.setText(clientSession.getCurrentUser().getPhoneNumber());
        emailInput.setText(clientSession.getCurrentUser().getEmail());
      }
      setVisible(isEditing);
    }
  }

  private void saveChange() {
    String fullName = fullNameInput.getText().trim();
    String phone = phoneInput.getText().trim();
    String email = emailInput.getText().trim();

    if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
      AlertUtil.showErrorAlert("Update failed", "All fields are required");
      return;
    }

    setLoadingState(true);
    editButton.setText("Saving...");

    clientService.updateProfile(fullName, email, phone)
      .thenAccept(response -> FXThread.run(() -> {
        setLoadingState(false);

        if (response != null && Response.OK.equals(response.getStatus())) {
          AlertUtil.showInfoAlert("Success", "Profile updated successfully!");

          isEditing = false;
          setVisible(isEditing);
        } else {
          String errorMsg = (response != null) ? response.getMessage() : "Failed to update profile";
          AlertUtil.showErrorAlert("Update failed", errorMsg);
          editButton.setText("Save");
        }
      }))
      .exceptionally(ex -> {
        FXThread.run(() -> {
          setLoadingState(false);
          editButton.setText("Save");
          AlertUtil.showErrorAlert("Network Error", "Cannot connect to server. Please try again.");
        });
        return null;
      });
  }

  private void setLoadingState(boolean isLoading) {
    editButton.setDisable(isLoading);
    fullNameInput.setDisable(isLoading);
    phoneInput.setDisable(isLoading);
    emailInput.setDisable(isLoading);
  }

  private void setVisible(boolean value) {
    if (value) {
      editButton.setText("Save");
    } else {
      editButton.setText("Edit Profile");
    }

    fullNameInput.setVisible(value);
    phoneInput.setVisible(value);
    emailInput.setVisible(value);

    fullNameLabel.setVisible(!value);
    phoneLabel.setVisible(!value);
    emailLabel.setVisible(!value);

    fullNameInput.setManaged(value);
    phoneInput.setManaged(value);
    emailInput.setManaged(value);

    fullNameLabel.setManaged(!value);
    phoneLabel.setManaged(!value);
    emailLabel.setManaged(!value);
  }
}