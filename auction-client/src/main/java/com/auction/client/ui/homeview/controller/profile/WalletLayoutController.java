package com.auction.client.ui.homeview.controller.profile;

import com.auction.client.navigation.SceneManager;
import com.auction.client.service.UserService;
import com.auction.client.store.userinformation.ClientSession;
import com.auction.client.store.userinformation.UserTransactionHistory;
import com.auction.client.ui.component.IntegerField;
import com.auction.client.ui.homeview.homeviewcomponent.TransactionHistory;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.StageUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class WalletLayoutController {
  private ClientSession clientSession;

  @FXML private Label balanceLabel;
  @FXML private IntegerField depositAmountField;

  public void setClientSession(ClientSession session) {
    unbind();
    this.clientSession = session;
    bind();
  }

  private void unbind() {
    balanceLabel.textProperty().unbind();
  }
  private void bind() {
    balanceLabel.textProperty().bind(clientSession.currentBalanceProperty().asString("$ %.2f"));
  }

  @FXML
  private void handleDeposit() {
    double balance = depositAmountField.getValue();
    if (balance <= 0) {
      AlertUtil.showErrorAlert("Deposit failed", "Balance must be greater than 0.");
      return;
    }

    String message = UserService.deposit(balance);
    System.out.println(message);
    if (message != null) {
      AlertUtil.showErrorAlert("Deposit failed", message);
    }
  }

  @FXML
  private void handleShowHistory() {
    TransactionHistory transactionHistory = new TransactionHistory();
    transactionHistory.setTransactionHistory(UserTransactionHistory.USER_TRANSACTION_HISTORY.getHistory());
    StageUtil.autoCloseModalStage(transactionHistory, SceneManager.getInstance().getWindow());
  }
}
