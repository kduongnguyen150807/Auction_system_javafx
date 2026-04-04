package com.auction.client.ui.SearchBar;

import com.auction.client.util.NotificationPopup;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import javafx.geometry.Point2D;

public class ThanhTimKiemController {
  @FXML private TextField searchField;
  @FXML private ComboBox<String> categoryFilter;
  @FXML private Button bellButton;
  private NotificationPopup ans;

  @FXML
  public void initialize() {
    ans = new NotificationPopup();
    // Nhét thêm code tìm kiếm cũ của mày vào dưới đây nếu có
  }

  @FXML
  public void toggleNotifications() {
    Window res = bellButton.getScene().getWindow();
    Point2D res1 = bellButton.localToScene(0.0, 0.0);
    // Tọa độ xổ xuống, chỉnh lại số nếu màn hình của mày nó bị lệch
    double res2 = res.getX() + res.getScene().getX() + res1.getX();
    double res3 = res.getY() + res.getScene().getY() + res1.getY() + bellButton.getHeight() + 10;
    ans.show(res, res2, res3);
  }
}