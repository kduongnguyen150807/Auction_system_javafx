package com.auction.client.ui.component.userbar;

import com.auction.client.store.userinformation.UserModel;
import javafx.util.Callback;

import java.util.function.Consumer;

public record UserBarConfig (
  Callback<UserModel, UserBar> cardFactory,
  Consumer<UserModel> onUserBarClicked
) {
  public UserBarConfig (Consumer<UserModel> onUserBarClicked) {
    this(
      userModel -> {
        UserBar userBar = new UserBar(userModel);
        userBar.setOnUserBarClicked(onUserBarClicked);

        return userBar;
      },
      onUserBarClicked
    );
  }
}
