package com.auction.client.ui.component;

import com.auction.client.store.userinformation.ClientSession;
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

  private ClientSession clientSession;

  @FXML private ImageView avatarImageView;
  @FXML private Label userNameLabel;
  @FXML private Label verifiedLabel;
  @FXML private Label ratingStarsLabel;
  @FXML private Label ratingCountLabel;
  @FXML private Label reputationWarning;

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

  public void setClientSession(ClientSession clientSession) {
    unbind();
    this.clientSession = clientSession;
    bind();
  }

  private void unbind() {
    userNameLabel.textProperty().unbind();
    verifiedLabel.textProperty().unbind();
    ratingStarsLabel.textProperty().unbind();
    ratingCountLabel.textProperty().unbind();
  }

  private void bind() {
    userNameLabel.textProperty().bind(clientSession.currentNameProperty());
    verifiedLabel.textProperty().bind(Bindings.createStringBinding(
      () -> {
        String phone = clientSession.phoneNumberProperty().get();
        boolean verified = phone != null && !phone.isBlank();
        return verified ? "Verified" : "Unverified";
      }, clientSession.phoneNumberProperty()));

    ratingStarsLabel.textProperty().bind(Bindings.createStringBinding(
      () ->
        StarUtils.stars(
          clientSession.averageRatingProperty().get()
        ), clientSession.averageRatingProperty()));

    ratingCountLabel.textProperty().bind(Bindings.createStringBinding(
      () -> {
        double averageRating = clientSession.averageRatingProperty().get();
        if (averageRating > 0) {
          if (averageRating < 2) {
            return "Negative";
          } else if (averageRating < 3) {
            return "Neutral";
          } else {
            return "Positive";
          }
        }
        return "No rating yet";
      },  clientSession.averageRatingProperty()));

    clientSession.avatarUrlProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null || newValue.isBlank()) {
        return;
      }

      loadAvatar(newValue);
    });
    loadAvatar(clientSession.avatarUrlProperty().get());
  }

  private void loadAvatar(String avatarUrl) {
    if (avatarUrl == null ||  avatarUrl.isEmpty()) {
      avatarImageView.setImage(null);
      return;
    }

    ImagePresentationUtil.loadCircularAvatar(avatarImageView, avatarUrl, 34, 68);
  }
}
