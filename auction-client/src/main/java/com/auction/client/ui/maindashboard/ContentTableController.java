package com.auction.client.ui.maindashboard;

import com.auction.client.ui.base.PageController;
import com.auction.client.ui.component.ContentButton;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller quản lý bảng các nút điều hướng (sidebar/menu) trong màn hình Home.
 *
 * <p>Chức năng chính:
 * <ul>
 *   <li>Tạo và hiển thị các {@link ContentButton} tương ứng với từng {@link HomeViewType}</li>
 *   <li>Quản lý trạng thái active/inactive của các button</li>
 *   <li>Gửi yêu cầu chuyển view thông qua callback {@code switchView}</li>
 * </ul>
 *
 * <p>Thiết kế:
 * <ul>
 *   <li>Sử dụng {@link EnumMap} để ánh xạ giữa {@link HomeViewType} và button tương ứng</li>
 *   <li>State UI (active button) được quản lý bởi controller thay vì JavaFX pseudo-class</li>
 * </ul>
 */
public class ContentTableController extends PageController<HomeViewType> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ContentTableController.class);

  @FXML
  private VBox ContentButtonTable;

  /** Map lưu trữ các button theo từng loại type */
  private final Map<HomeViewType, ContentButton<HomeViewType>> contentButtonMap = new EnumMap<>(HomeViewType.class);

  /**
   * Được gọi tự động sau khi FXML được load và các field được inject.
   */
  public void initialize() {
    initNode();
    setDefaultActive();
  }

  /**
   * Khởi tạo các button điều hướng.
   */
  private void initNode() {
    registerButton("Auction", HomeViewType.AUCTION);
    registerButton("Profile",  HomeViewType.PROFILE);
  }

  /**
   * Thiết lập button mặc định ở trạng thái active.
   */
  private void setDefaultActive() {
    setActive(contentButtonMap.get(HomeViewType.AUCTION));
  }

  /**
   * Tạo một button điều hướng và đăng ký vào hệ thống.
   *
   * @param text Nội dung hiển thị trên button
   * @param type Loại view tương ứng khi click
   */
  private void registerButton(String text, HomeViewType type) {
    ContentButton<HomeViewType> button = new ContentButton<>(text, type);
    button.setOnAction(event -> {
      setActive(button);
      switchView.accept(button.getType());
    });
    contentButtonMap.put(type, button);
    ContentButtonTable.getChildren().add(button);
  }

  /**
   * Xử lý khi người dùng click vào button.
   *
   * @param selected Button được click
   */
  private void setActive(ContentButton<HomeViewType> selected) {
    ContentButtonTable.getChildren().forEach(node -> {
      node.getStyleClass().setAll("nav-button", "nav-button-inactive");
    });

    selected.getStyleClass().setAll("nav-button", "nav-button-active");

    LOGGER.info("set active button to {}", selected);
  }

  @FXML
  private void handlePrimaryAction() {}
}
