package com.auction.client.ui.component;

import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.userinformation.UserModel;
import com.auction.client.util.ImagePresentationUtil;
import com.auction.client.util.StarUtils;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class UserCard extends HBox {
  private static final String USER_CARD_FXML = "/fxml/component/UserCard.fxml";

  private UserModel userModel;

  @FXML private ImageView avatarImageView;
  @FXML private Label userNameLabel;
  @FXML private Label verifiedLabel;
  @FXML private Label ratingStarsLabel;

  public UserCard() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(USER_CARD_FXML));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);

    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setUserModel(UserModel userModel) {
    unbind();
    this.userModel = userModel;
    bind();
  }

  private void unbind() {
    userNameLabel.textProperty().unbind();
    verifiedLabel.textProperty().unbind();
    ratingStarsLabel.textProperty().unbind();
  }

  private void bind() {
    userNameLabel.textProperty().bind(userModel.usernameProperty());
    verifiedLabel.textProperty().bind(Bindings.createStringBinding(
      () -> {
        String phone = userModel.phoneNumberProperty().get();
        boolean verified = phone != null && !phone.isBlank();
        return verified ? "Verified" : "Unverified";
      }, userModel.phoneNumberProperty()));

    ratingStarsLabel.textProperty().bind(userModel.avgRatingProperty());

    userModel.avatarUrlProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null || newValue.isBlank()) {
        return;
      }

      loadAvatar(newValue);
    });
    loadAvatar(userModel.avatarUrlProperty().get());
  }

  private void loadAvatar(String avatarUrl) {
    if (avatarUrl == null ||  avatarUrl.isEmpty()) {
      avatarImageView.setImage(null);
      return;
    }

    ImagePresentationUtil.loadCircularAvatar(avatarImageView, avatarUrl, 34, 68);
  }

  private void addOrRemove(Label label, String text, String preText) {
    if (text == null || text.isEmpty()) {
      label.setVisible(false);
      label.setManaged(false);
    } else {
      label.setText(preText + text);
    }
  }
}
