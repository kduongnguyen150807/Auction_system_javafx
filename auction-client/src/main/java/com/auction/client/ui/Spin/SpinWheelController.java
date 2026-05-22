package com.auction.client.ui.Spin;

import com.auction.client.ClientSession;
import com.auction.client.service.SpinWheelClientService;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.SpinWheelResult;
import com.auction.shared.SpinWheelSegmentInfo;
import com.auction.shared.SpinWheelState;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SpinWheelController {

  @FXML private StackPane wheelHost;
  @FXML private Label statusLabel;
  @FXML private Label countdownLabel;
  @FXML private Label creditsLabel;
  @FXML private Label balanceLabel;
  @FXML private Button spinButton;
  @FXML private Button buyButton;

  private final SpinWheelClientService spinService = new SpinWheelClientService();
  private SpinWheelPane wheelPane;
  private SpinWheelState currentState;
  private Timeline countdownTimeline;
  private boolean spinning;

  @FXML
  public void initialize() {
    wheelPane = new SpinWheelPane();
    if (wheelHost != null) {
      wheelHost.getChildren().add(wheelPane);
    }
    countdownTimeline =
        new Timeline(
            new KeyFrame(
                javafx.util.Duration.seconds(1),
                e -> Platform.runLater(this::updateCountdown)));
    countdownTimeline.setCycleCount(Timeline.INDEFINITE);
    countdownTimeline.play();
  }

  public void refreshOnNavigate() {
    loadState();
  }

  @FXML
  private void handleSpin() {
    if (spinning) {
      return;
    }
    spinning = true;
    setButtonsDisabled(true);
    if (statusLabel != null) {
      statusLabel.setText("Đang quay...");
    }

    new Thread(
            () -> {
              SpinWheelResult result = spinService.spin();
              Platform.runLater(
                  () -> {
                    if (result == null) {
                      finishSpinWithError("Không thể kết nối server.");
                      return;
                    }
                    if (result.getUser() == null) {
                      finishSpinWithError(result.getMessage());
                      applyResultState(result);
                      return;
                    }
                    int index = result.getSegmentIndex();
                    wheelPane.spinToSegment(
                        index,
                        () -> {
                          KhungController.refreshSidebarFromSession();
                          if (statusLabel != null) {
                            statusLabel.setText(result.getMessage());
                          }
                          applyResultState(result);
                          spinning = false;
                          setButtonsDisabled(false);
                        });
                  });
            })
        .start();
  }

  @FXML
  private void handleBuyCredit() {
    if (spinning) {
      return;
    }
    new Thread(
            () -> {
              SpinWheelState state = spinService.buyCredits(1);
              Platform.runLater(
                  () -> {
                    if (state != null) {
                      applyState(state);
                      KhungController.refreshSidebarFromSession();
                      if (statusLabel != null) {
                        statusLabel.setText("Đã mua thêm 1 lượt quay!");
                      }
                    } else if (statusLabel != null) {
                      statusLabel.setText("Mua lượt quay thất bại — kiểm tra số dư.");
                    }
                  });
            })
        .start();
  }

  @FXML
  private void handleRefresh() {
    loadState();
  }

  private void loadState() {
    new Thread(
            () -> {
              SpinWheelState state = spinService.fetchState();
              Platform.runLater(
                  () -> {
                    if (state != null) {
                      applyState(state);
                    }
                  });
            })
        .start();
  }

  private void applyState(SpinWheelState state) {
    currentState = state;
    List<String> labels = new ArrayList<>();
    if (state.getSegments() != null) {
      for (SpinWheelSegmentInfo info : state.getSegments()) {
        labels.add(info.getLabel());
      }
    }
    wheelPane.setSegmentLabels(labels);
    updateCountdown();
    updateCreditsUi();
    updateBalanceUi();
    updateSpinButton();
  }

  private void applyResultState(SpinWheelResult result) {
    if (currentState == null) {
      currentState = new SpinWheelState();
    }
    currentState.setFreeSpinAvailable(result.isFreeSpinAvailable());
    currentState.setNextFreeSpinAt(result.getNextFreeSpinAt());
    currentState.setPaidSpinCredits(result.getPaidSpinCredits());
    updateCountdown();
    updateCreditsUi();
    updateBalanceUi();
    updateSpinButton();
  }

  private void updateCountdown() {
    if (countdownLabel == null || currentState == null) {
      return;
    }
    if (currentState.isFreeSpinAvailable()) {
      countdownLabel.setText("Lượt quay miễn phí: Sẵn sàng!");
      return;
    }
    LocalDateTime next = currentState.getNextFreeSpinAt();
    if (next == null) {
      countdownLabel.setText("Lượt quay miễn phí: —");
      return;
    }
    Duration remaining = Duration.between(LocalDateTime.now(), next);
    if (remaining.isNegative() || remaining.isZero()) {
      countdownLabel.setText("Lượt quay miễn phí: Sẵn sàng!");
      currentState.setFreeSpinAvailable(true);
      updateSpinButton();
      return;
    }
    long h = remaining.toHours();
    long m = remaining.toMinutesPart();
    long s = remaining.toSecondsPart();
    countdownLabel.setText(
        String.format("Lượt miễn phí sau: %02d:%02d:%02d (00:00 ngày mai)", h, m, s));
  }

  private void updateCreditsUi() {
    if (creditsLabel == null || currentState == null) {
      return;
    }
    creditsLabel.setText("Lượt đã mua: " + currentState.getPaidSpinCredits());
  }

  private void updateBalanceUi() {
    if (balanceLabel == null) {
      return;
    }
    if (ClientSession.getCurrentUser() != null) {
      balanceLabel.setText(
          String.format("Số dư: %,.0f$", ClientSession.getCurrentUser().getBalance()));
    }
  }

  private void updateSpinButton() {
    if (spinButton == null || currentState == null) {
      return;
    }
    boolean canSpin = currentState.isFreeSpinAvailable() || currentState.getPaidSpinCredits() > 0;
    spinButton.setDisable(!canSpin || spinning);
    if (currentState.isFreeSpinAvailable()) {
      spinButton.setText("QUAY MIỄN PHÍ");
    } else if (currentState.getPaidSpinCredits() > 0) {
      spinButton.setText("QUAY (" + currentState.getPaidSpinCredits() + " lượt)");
    } else {
      spinButton.setText("HẾT LƯỢT QUAY");
    }
    if (buyButton != null) {
      buyButton.setText(
          String.format("Mua 1 lượt (%,.0f$)", currentState.getSpinCreditPrice()));
    }
  }

  private void finishSpinWithError(String msg) {
    if (statusLabel != null) {
      statusLabel.setText(msg);
    }
    spinning = false;
    setButtonsDisabled(false);
    updateSpinButton();
  }

  private void setButtonsDisabled(boolean disabled) {
    if (spinButton != null) {
      spinButton.setDisable(disabled || (currentState != null && !canSpin()));
    }
    if (buyButton != null) {
      buyButton.setDisable(disabled);
    }
  }

  private boolean canSpin() {
    return currentState != null
        && (currentState.isFreeSpinAvailable() || currentState.getPaidSpinCredits() > 0);
  }
}
