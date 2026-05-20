package com.auction.client.store.clientinformation;

import com.auction.shared.TransactionLog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class UserTransactionHistory {
  public static UserTransactionHistory USER_TRANSACTION_HISTORY = new UserTransactionHistory();

  private UserTransactionHistory() {}

  private ObservableList<TransactionLog>  history = FXCollections.observableArrayList();

  public ObservableList<TransactionLog> getHistory() {
    return history;
  }

  public void setHistory(List<TransactionLog> history) {
    this.history.clear();
    this.history.addAll(history);
  }

  public void addTransaction(TransactionLog transactionLog) {
    history.add(transactionLog);
  }
}
