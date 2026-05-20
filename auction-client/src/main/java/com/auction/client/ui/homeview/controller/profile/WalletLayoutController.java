package com.auction.client.ui.homeview.controller.profile;

import com.auction.client.app.AutoInject;
import com.auction.client.navigation.SceneManager;
import com.auction.client.service.user.ClientService;
import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.store.clientinformation.UserTransactionHistory;
import com.auction.client.ui.component.IntegerField;
import com.auction.client.ui.homeview.homeviewcomponent.TransactionHistory;
import com.auction.client.util.AlertUtil;
import com.auction.client.util.FXThread;
import com.auction.client.util.StageUtil;
import com.auction.shared.Response;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class WalletLayoutController {
  private ClientSession clientSession;

  @FXML private Label balanceLabel;
  @FXML private IntegerField depositAmountField;

  private final ClientService clientService;

  @AutoInject
  public WalletLayoutController(ClientService clientService) {
    this.clientService = clientService;
  }

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
    double amount = depositAmountField.getValue();
    if (amount <= 0) {
      AlertUtil.showErrorAlert("Deposit failed", "Balance must be greater than 0.");
      return;
    }

    clientService.deposit(amount)
      .thenAccept(response -> FXThread.run(() -> {

        if (response != null && Response.OK.equals(response.getStatus())) {
          AlertUtil.showInfoAlert("Success", "Deposited $ " + String.format("%.2f", amount) + " successfully!");
          depositAmountField.clear();
        } else {
          String errorMsg = (response != null) ? response.getMessage() : "Fail to deposit amount";
          AlertUtil.showErrorAlert("Deposit failed", errorMsg);
        }
      }))
      .exceptionally(ex -> {
        FXThread.run(() -> {
          AlertUtil.showErrorAlert("Network Error", "Cannot connect to server. Please try again.");
        });
        return null;
      });
  }

  @FXML
  private void handleShowHistory() {
    TransactionHistory transactionHistory = new TransactionHistory();
    transactionHistory.setTransactionHistory(UserTransactionHistory.USER_TRANSACTION_HISTORY.getHistory());
    StageUtil.autoCloseModalStage(transactionHistory, SceneManager.getInstance().getWindow());
  }
}
