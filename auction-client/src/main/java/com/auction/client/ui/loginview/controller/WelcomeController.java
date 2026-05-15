package com.auction.client.ui.loginview.controller;

import com.auction.client.ui.base.CanSwitchNode;
import com.auction.client.ui.loginview.LoginViewType;
import javafx.event.ActionEvent;

import java.util.function.Consumer;

public class WelcomeController implements CanSwitchNode<LoginViewType> {
  private Consumer<LoginViewType> switchNode;

  @Override
  public void setSwitchNode(Consumer<LoginViewType> switchNode) {
    this.switchNode = switchNode;
  }

  public void toLogin(ActionEvent e) throws Exception {
    switchNode.accept(LoginViewType.LOGIN);
  }

  public void toRegister(ActionEvent e) throws Exception {
    switchNode.accept(LoginViewType.REGISTER);
  }
}
