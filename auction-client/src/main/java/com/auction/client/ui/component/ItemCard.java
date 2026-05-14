package com.auction.client.ui.component;

import com.auction.client.ui.utils.TimeUI;
import com.auction.shared.item.Item;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Component đại diện cho thẻ hiển thị thông tin của một vật phẩm đấu giá.
 *
 * <p>{@code ItemCard} sử dụng JavaFX FXML để định nghĩa giao diện
 * và kế thừa từ {@link VBox} nhằm dễ dàng tích hợp vào các layout khác.
 *
 * <p>Component này thường được sử dụng trong:
 * <ul>
 *   <li>Danh sách sản phẩm đấu giá</li>
 *   <li>Dashboard</li>
 *   <li>Kết quả tìm kiếm</li>
 *   <li>Màn hình đấu giá đang diễn ra</li>
 * </ul>
 *
 * <p>Khi khởi tạo, component sẽ:
 * <ul>
 *   <li>Nạp file FXML định nghĩa giao diện</li>
 *   <li>Thiết lập controller hiện tại</li>
 *   <li>Áp dụng stylesheet mặc định</li>
 * </ul>
 */
public class ItemCard extends VBox {
  private static final String BASE_FXML_PATH = "/fxml/Component/ItemCard.fxml";
  private static final String BASE_STYLESHEET_PATH = "/css/Component/ItemCard.css";

  @FXML private Label ItemName;
  @FXML private Label ItemDescription;
  @FXML private Label Price;
  @FXML private Label TimeRemain;

  private final Item item;

  /**
   * Khởi tạo component ItemCard và nạp giao diện cơ sở.
   */
  public ItemCard(Item item) {
    initBaseLayout();
    setData(item);
    this.item = item;
  }

  /**
   * Khởi tạo giao diện cơ sở cho component từ file FXML.
   *
   * <p>Phương thức này chịu trách nhiệm:
   * <ul>
   *   <li>Tạo FXMLLoader</li>
   *   <li>Thiết lập root và controller</li>
   *   <li>Nạp stylesheet</li>
   *   <li>Load giao diện từ file FXML</li>
   * </ul>
   *
   * @throws RuntimeException nếu xảy ra lỗi khi load giao diện
   */
  private void initBaseLayout() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource(BASE_FXML_PATH));
    loader.setRoot(this);
    loader.setController(this);

    this.getStylesheets().add(BASE_STYLESHEET_PATH);
    try {
      loader.load();
    } catch (IOException e) {
      throw new RuntimeException("Critical UI load failure", e);
    }
  }

  public void setData(Item item) {
    ItemName.setText(item.getName());
    ItemDescription.setText(item.getDescription());
    Price.setText(String.valueOf(item.getCurrentPrice()));
    TimeRemain.setText(TimeUI.getRemainingTime(item.getEndTime()));
  }

  public Item getItem() {
    return item;
  }
}
