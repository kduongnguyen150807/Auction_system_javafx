package com.auction.client.ui.homeview.controller.profile;

import com.auction.client.store.clientinformation.ClientSession;
import com.auction.client.ui.component.MetricCard;
import com.auction.shared.UserRole;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class MetricLayoutController {
  private ClientSession clientSession;

  private boolean isBidder = true;

  @FXML private Label roleLabel;
  @FXML private Button toggleRoleButton;
  @FXML private HBox bidderMetricsRow;
  @FXML private HBox sellerMetricsRow;

  @FXML private MetricCard moneySpentMetric;
  @FXML private MetricCard itemsBoughtMetric;
  @FXML private MetricCard moneyReceivedMetric;
  @FXML private MetricCard itemsSoldMetric;

  public void  setClientSession(ClientSession clientSession) {
    unbind();
    this.clientSession = clientSession;
    bind();
  }

  private void unbind() {
    moneySpentMetric.unbind();
    itemsBoughtMetric.unbind();
    itemsSoldMetric.unbind();
    moneyReceivedMetric.unbind();

    roleLabel.textProperty().unbind();
  }

  private void bind() {
    moneySpentMetric.bind(clientSession.moneySpentProperty().asString("$ %.2f"));
    itemsBoughtMetric.bind(clientSession.itemsBoughtProperty().asString());
    itemsSoldMetric.bind(clientSession.itemsSoldProperty().asString());
    moneyReceivedMetric.bind(clientSession.moneyReceivedProperty().asString("$ %.2f"));

    roleLabel.textProperty().bind(clientSession.currentRoleProperty().asString());
  }

  public void handleToggleRole(ActionEvent actionEvent) {
    if (isBidder) {
      toggleRoleButton.setText("Đổi sang Bidder");
      bidderMetricsRow.setVisible(false);
      sellerMetricsRow.setVisible(true);
      bidderMetricsRow.setManaged(false);
      sellerMetricsRow.setManaged(true);
      isBidder = false;

      clientSession.currentRoleProperty().setValue(UserRole.SELLER);
    } else {
      bidderMetricsRow.setVisible(true);
      sellerMetricsRow.setVisible(false);
      bidderMetricsRow.setManaged(true);
      sellerMetricsRow.setManaged(false);
      toggleRoleButton.setText("Đổi sang Seller");
      isBidder = true;

      clientSession.currentRoleProperty().setValue(UserRole.BIDDER);
    }
  }
}
