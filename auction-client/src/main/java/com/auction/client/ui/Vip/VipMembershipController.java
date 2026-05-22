package com.auction.client.ui.Vip;

import com.auction.client.ClientSession;
import com.auction.client.service.UserAccountService;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.User;
import com.auction.shared.VipPlan;
import com.auction.shared.VipPlanInfo;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class VipMembershipController {

  private static final DateTimeFormatter VIP_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  @FXML private Label statusLabel;
  @FXML private Label balanceLabel;
  @FXML private Label vipStatusLabel;
  @FXML private GridPane plansGrid;

  private final UserAccountService userAccountService = new UserAccountService();

  @FXML
  public void initialize() {
    refreshView();
  }

  public void refreshOnNavigate() {
    refreshView();
  }

  @FXML
  private void handleRefresh() {
    User current = ClientSession.getCurrentUser();
    if (current == null) {
      return;
    }
    User refreshed = userAccountService.refreshUser(current.getId());
    if (refreshed != null) {
      ClientSession.setCurrentUser(refreshed);
      KhungController.refreshSidebarFromSession();
    }
    refreshView();
  }

  private void refreshView() {
    User user = ClientSession.getCurrentUser();
    if (user == null) {
      return;
    }
    if (balanceLabel != null) {
      balanceLabel.setText(String.format("Số dư: %,.0f$", user.getBalance()));
    }
    if (vipStatusLabel != null) {
      if (user.isVip()) {
        vipStatusLabel.setText("VIP đến: " + user.getVipUntil().format(VIP_FMT));
        vipStatusLabel.getStyleClass().removeAll("vip-inactive");
        if (!vipStatusLabel.getStyleClass().contains("vip-active")) {
          vipStatusLabel.getStyleClass().add("vip-active");
        }
      } else {
        vipStatusLabel.setText("Bạn đang là thành viên thường");
        vipStatusLabel.getStyleClass().removeAll("vip-active");
        if (!vipStatusLabel.getStyleClass().contains("vip-inactive")) {
          vipStatusLabel.getStyleClass().add("vip-inactive");
        }
      }
    }
    renderPlans(user);
    if (statusLabel != null) {
      statusLabel.setText("");
    }
  }

  private void renderPlans(User user) {
    if (plansGrid == null) {
      return;
    }
    plansGrid.getChildren().clear();
    List<VipPlanInfo> plans = VipPlan.allPlans();
    int col = 0;
    for (VipPlanInfo plan : plans) {
      plansGrid.add(buildPlanCard(plan, user), col++, 0);
    }
  }

  private VBox buildPlanCard(VipPlanInfo plan, User user) {
    Label title = new Label(plan.getLabel());
    title.getStyleClass().add("vip-plan-title");

    Label price = new Label(String.format("%,.0f$", plan.getPrice()));
    price.getStyleClass().add("vip-plan-price");

    Label duration = new Label(plan.getDays() + " ngày");
    duration.getStyleClass().add("vip-plan-duration");

    Button buy = new Button("Mua VIP");
    buy.getStyleClass().add("btn-vip-buy");
    buy.setMaxWidth(Double.MAX_VALUE);
    buy.setOnAction(e -> purchasePlan(plan.getId()));

    VBox card = new VBox(10, title, price, duration, buy);
    card.getStyleClass().add("vip-plan-card");
    card.setAlignment(Pos.CENTER);
    card.setPadding(new Insets(20));
    card.setPrefWidth(200);
    card.setUserData(plan.getId());

    boolean affordable = user.getBalance() >= plan.getPrice();
    buy.setDisable(!affordable);
    if (!affordable) {
      buy.setText("Không đủ số dư");
    }
    return card;
  }

  private void purchasePlan(String planId) {
    if (statusLabel != null) {
      statusLabel.setText("Đang xử lý...");
    }
    new Thread(
            () -> {
              boolean ok = userAccountService.purchaseVip(planId);
              Platform.runLater(
                  () -> {
                    if (ok) {
                      KhungController.refreshSidebarFromSession();
                      com.auction.client.ui.Live.LiveAuctionController.refreshLocalVipIfOpen();
                      if (statusLabel != null) {
                        statusLabel.setText("Mua VIP thành công!");
                      }
                    } else if (statusLabel != null) {
                      statusLabel.setText("Mua VIP thất bại — kiểm tra số dư.");
                    }
                    refreshView();
                  });
            })
        .start();
  }
}
