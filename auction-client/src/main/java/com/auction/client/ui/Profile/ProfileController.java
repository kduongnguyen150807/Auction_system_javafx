package com.auction.client.ui.Profile;

import com.auction.client.ClientSession;
import com.auction.client.SceneManager;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.UserRole;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ProfileController {
    @FXML private Label usernameLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label roleLabel;
    @FXML private TextField fullNameInput;
    @FXML private TextField emailInput;
    @FXML private TextField phoneInput;
    @FXML private Button editButton;
    @FXML private Button toggleRoleButton;

    private boolean editing = false;

    @FXML
    public void initialize() {
        refreshData();
        setEditingMode(false);
    }

    @FXML
    public void handleEditProfile() {
        if (!editing) {
            setEditingMode(true);
            return;
        }
        ClientSession.updateProfile(
                fullNameInput.getText(),
                emailInput.getText(),
                phoneInput.getText()
        );
        refreshData();
        setEditingMode(false);
        KhungController.refreshSidebarFromSession();
    }

    @FXML
    public void handleToggleRole() {
        ClientSession.toggleRole();
        refreshData();
        KhungController.refreshSidebarFromSession();
    }

    @FXML
    public void handleLogout() {
        ClientSession.clear();
        try {
            SceneManager.switchscene("/fxml/welcome.fxml");
        } catch (IOException ignored) {
        }
    }

    public void refreshData() {
        String username = fallback(ClientSession.getUsername(), "username");
        String fullName = fallback(ClientSession.getFullName(), username);
        String email = fallback(ClientSession.getEmail(), "username@mail.com");
        String phone = fallback(ClientSession.getPhone(), "N/A");
        UserRole role = ClientSession.getActiveRole();

        usernameLabel.setText(username);
        fullNameLabel.setText(fullName);
        emailLabel.setText(email);
        phoneLabel.setText(phone);
        roleLabel.setText(toTitle(role.name()));

        fullNameInput.setText(fullName);
        emailInput.setText(email);
        phoneInput.setText(phone);

        toggleRoleButton.setText(role == UserRole.SELLER ? "Switch to Bidder" : "Switch to Seller");
    }

    private void setEditingMode(boolean value) {
        editing = value;
        editButton.setText(editing ? "Save Profile" : "Edit Profile");

        fullNameLabel.setVisible(!editing);
        fullNameLabel.setManaged(!editing);
        emailLabel.setVisible(!editing);
        emailLabel.setManaged(!editing);
        phoneLabel.setVisible(!editing);
        phoneLabel.setManaged(!editing);

        fullNameInput.setVisible(editing);
        fullNameInput.setManaged(editing);
        emailInput.setVisible(editing);
        emailInput.setManaged(editing);
        phoneInput.setVisible(editing);
        phoneInput.setManaged(editing);
    }

    private String fallback(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String toTitle(String role) {
        String lower = role.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
