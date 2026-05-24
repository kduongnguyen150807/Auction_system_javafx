package com.auction.client.ui.component.userbar;

import com.auction.client.store.userinformation.UserModel;
import com.auction.client.ui.base.CanBind;
import com.auction.client.util.ImagePresentationUtil;
import com.auction.client.util.StringFormat;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.function.Consumer;

public class UserBar extends HBox {
  private static final String BASE_FXML_PATH = "/fxml/component/UserBar.fxml";

  private UserModel userModel;

  @FXML private ImageView avatarImageView;
  @FXML private Label userNameLabel;
  @FXML private Label moneySpent;
  @FXML private Label ratingStarsLabel;
  @FXML private Label topLabel;

  private Consumer<UserModel> onUserBarClicked;

  public UserBar(UserModel userModel) {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setController(this);
    fxmlLoader.setRoot(this);
    try {
      fxmlLoader.load();
      this.userModel = userModel;
      bind();
      setOnMouseClicked(event -> {
        if (onUserBarClicked != null) {
          onUserBarClicked.accept(userModel);
        }
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void bind() {
    userNameLabel.setText(userModel.usernameProperty().getValue());
    moneySpent.setText("Money spent: " + StringFormat.formatMoney(userModel.moneySpentProperty().get()));
    ratingStarsLabel.textProperty().bind(userModel.avgRatingProperty());

    if (userModel.avatarUrlProperty().getValue() != null) {
      loadUserAvatar(userModel.avatarUrlProperty().get());
    }

    setRank(userModel.getRank());
  }

  private void loadUserAvatar(String url) {
    if (url == null ||  url.isEmpty()) {
      return;
    }

    ImagePresentationUtil.loadCircularAvatar(avatarImageView, url, 16, 32);
  }

  public void setRank(int rank) {
    if (rank == 1) {
      topLabel.setText("🥇"); // Top 1 dùng Huy chương vàng
    } else if (rank == 2) {
      topLabel.setText("🥈"); // Top 2 Huy chương bạc
    } else if (rank == 3) {
      topLabel.setText("🥉"); // Top 3 Huy chương đồng
    } else {
      topLabel.setText("#" + rank); // Các hạng sau hiển thị #4, #5...
    }
  }

  public void setOnUserBarClicked(Consumer<UserModel> onUserBarClicked) {
    this.onUserBarClicked = onUserBarClicked;
  }
}
