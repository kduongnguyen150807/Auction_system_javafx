package com.auction.client.ui.component.userbar;

import com.auction.client.store.userinformation.UserModel;
import com.auction.client.util.ImagePresentationUtil;
import com.auction.client.util.StringFormat;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
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

  @FXML private HBox actionBox;
  @FXML private Button addFriendBtn;
  @FXML private Button acceptBtn;
  @FXML private Button declineBtn;

  private Consumer<UserModel> onUserBarClicked;

  private Runnable onAddFriendAction;
  private Runnable onAcceptAction;
  private Runnable onDeclineAction;

  public UserBar(UserModel userModel) {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    fxmlLoader.setController(this);
    fxmlLoader.setRoot(this);
    try {
      fxmlLoader.load();
      this.userModel = userModel;
      bind();
      initButtonEvents();

      setOnMouseClicked(event -> {
        if (onUserBarClicked != null) {
          onUserBarClicked.accept(userModel);
        }
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void initButtonEvents() {
    addFriendBtn.setOnAction(e -> {
      e.consume();
      if (onAddFriendAction != null) onAddFriendAction.run();
    });
    acceptBtn.setOnAction(e -> {
      e.consume();
      if (onAcceptAction != null) onAcceptAction.run();
    });
    declineBtn.setOnAction(e -> {
      e.consume();
      if (onDeclineAction != null) onDeclineAction.run();
    });
  }

  public void setDisplayMode(UserBarMode mode) {
    // Ẩn tất cả các nút đi trước để đưa về trạng thái sạch
    addFriendBtn.setVisible(false); addFriendBtn.setManaged(false);
    acceptBtn.setVisible(false);    acceptBtn.setManaged(false);
    declineBtn.setVisible(false);   declineBtn.setManaged(false);
    actionBox.setVisible(true);     actionBox.setManaged(true);

    if (mode == null) mode = UserBarMode.NONE;

    switch (mode) {
      case STRANGER -> {
        addFriendBtn.setVisible(true);
        addFriendBtn.setManaged(true);
        ratingStarsLabel.setVisible(false);
        ratingStarsLabel.setManaged(false);
        moneySpent.setVisible(false);
        moneySpent.setManaged(false);
      }
      case REQUEST -> {
        acceptBtn.setVisible(true);  acceptBtn.setManaged(true);
        declineBtn.setVisible(true); declineBtn.setManaged(true);
        ratingStarsLabel.setVisible(false);
        ratingStarsLabel.setManaged(false);
        moneySpent.setVisible(false);
        moneySpent.setManaged(false);
      }
      case NONE -> {
        actionBox.setVisible(false);
        actionBox.setManaged(false);
      }
      case FRIEND -> {
        actionBox.setVisible(false);
        actionBox.setManaged(false);
        ratingStarsLabel.setVisible(false);
        ratingStarsLabel.setManaged(false);
        moneySpent.setVisible(false);
        moneySpent.setManaged(false);
      }
    }
  }

  public void bind() {
    addOrRemove(userNameLabel, userModel.usernameProperty().get(), "");
    addOrRemove(moneySpent, StringFormat.formatMoney(userModel.moneySpentProperty().get()), "Money spent: ");
    addOrRemove(ratingStarsLabel, userModel.avgRatingProperty().get(), "");

    if (userModel.avatarUrlProperty().getValue() != null) {
      loadUserAvatar(userModel.avatarUrlProperty().get());
    }
    setRank(userModel.getRank());
  }

  private void loadUserAvatar(String url) {
    if (url == null || url.isEmpty()) return;
    ImagePresentationUtil.loadCircularAvatar(avatarImageView, url, 16, 32);
  }

  public void setRank(int rank) {
    if (rank == 0) {
      topLabel.setVisible(false);
      topLabel.setManaged(false);
      return;
    }
    topLabel.setVisible(true);
    topLabel.setManaged(true);
    if (rank == 1) topLabel.setText("🥇");
    else if (rank == 2) topLabel.setText("🥈");
    else if (rank == 3) topLabel.setText("🥉");
    else topLabel.setText("#" + rank);
  }

  private void addOrRemove(Label label, String text, String preText) {
    if (text == null || text.isEmpty()) {
      label.setVisible(false);
      label.setManaged(false);
    } else {
      label.setVisible(true);
      label.setManaged(true);
      label.setText(preText + text);
    }
  }

  // ════════════ CÁC HÀM TIỆN ÍCH SETTER CHO CONTROLLER ════════════
  public void setOnAddFriend(Runnable action) { this.onAddFriendAction = action; }
  public void setOnAccept(Runnable action) { this.onAcceptAction = action; }
  public void setOnDecline(Runnable action) { this.onDeclineAction = action; }

  public void setOnUserBarClicked(Consumer<UserModel> onUserBarClicked) {
    this.onUserBarClicked = onUserBarClicked;
  }
}
