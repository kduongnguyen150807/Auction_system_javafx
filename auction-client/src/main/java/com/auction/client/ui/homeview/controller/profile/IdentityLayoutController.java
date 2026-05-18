package com.auction.client.ui.homeview.controller.profile;

import com.auction.client.service.UserService;
import com.auction.client.store.ClientSession;
import com.auction.client.util.AlertUtil;
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

  boolean isEditing = false;

  private ClientSession clientSession;
  
  public void setClientSession(ClientSession session) {
    unbind();
    clientSession = session;
    bind();
  }

  private void unbind() {
    isEditing = false;
    setVisible(isEditing);

    fullNameLabel.textProperty().unbind();
    phoneLabel.textProperty().unbind();
    emailLabel.textProperty().unbind();
  }

  private void bind() {
    fullNameLabel.textProperty().bind(clientSession.currentNameProperty());
    phoneLabel.textProperty().bind(clientSession.phoneNumberProperty());
    emailLabel.textProperty().bind(clientSession.emailProperty());
  }
  
  @FXML
  private void handleEditProfile() {
    if (isEditing) {
      saveChange();
      isEditing = false;
      setVisible(isEditing);
    } else {
      isEditing = true;
      setVisible(isEditing);
    }
  }

  private void saveChange() {
    String fullName = fullNameInput.getText();
    String phone = phoneInput.getText();
    String email = emailInput.getText();

    if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty()) {
      AlertUtil.showErrorAlert("Update failed", "All fields are required");
      return;
    }

    String message = UserService.updateProfile(fullName, phone, email);
    if (message != null) {
      AlertUtil.showErrorAlert("Update failed", message);
    }
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
