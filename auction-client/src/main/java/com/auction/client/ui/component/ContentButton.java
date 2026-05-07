package com.auction.client.ui.component;

import javafx.scene.control.Button;


/**
 * Thành phần button mở rộng từ {@link Button},
 * dùng để đại diện cho một nội dung hoặc chức năng cụ thể
 * trong giao diện ứng dụng.
 *
 * <p>{@code ContentButton} cho phép gắn kèm một giá trị kiểu generic
 * nhằm xác định loại nội dung hoặc hành động mà button đại diện.
 *
 * <p>Class này thường được sử dụng cho:
 * <ul>
 *   <li>Thanh điều hướng (navigation)</li>
 *   <li>Menu chức năng</li>
 *   <li>Chuyển đổi giữa các màn hình</li>
 * </ul>
 *
 * <p>Khi khởi tạo, button sẽ tự động:
 * <ul>
 *   <li>Nạp stylesheet mặc định</li>
 *   <li>Áp dụng CSS class cho trạng thái navigation</li>
 *   <li>Thiết lập chiều rộng mặc định</li>
 * </ul>
 *
 * @param <T> kiểu dữ liệu đại diện cho loại nội dung hoặc hành động
 */
public class ContentButton<T> extends Button {
  private final String BASE_STYLESHEET_PATH = "/css/Component/ContentButton.css";

  /**
   * Giá trị đại diện cho loại nội dung hoặc chức năng của button.
   */
  private final T type;

  /**
   * Khởi tạo button với nội dung hiển thị và loại dữ liệu tương ứng.
   *
   * @param text nội dung hiển thị trên button
   * @param type loại nội dung hoặc hành động mà button đại diện
   */
  public ContentButton(String text, T type) {
    super(text);
    this.type = type;
    this.getStylesheets().add(ContentButton.class.getResource(BASE_STYLESHEET_PATH).toExternalForm());
    this.getStyleClass().addAll("nav-button", "nav-button-inactive");
    this.setPrefWidth(200);
  }


  /**
   * Trả về loại nội dung hoặc hành động của button.
   *
   * @return giá trị type của button
   */
  public T getType() {
    return type;
  }
}
