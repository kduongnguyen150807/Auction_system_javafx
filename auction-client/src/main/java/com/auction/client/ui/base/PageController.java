package com.auction.client.ui.base;

import java.util.function.Consumer;


/**
 * Lớp cơ sở cho các controller quản lý việc chuyển đổi giao diện (view/page)
 * trong ứng dụng JavaFX.
 *
 * <p>Lớp này cung cấp cơ chế dùng {@link Consumer} để thực hiện
 * điều hướng giữa các màn hình mà không làm controller phụ thuộc
 * trực tiếp vào hệ thống điều hướng cụ thể.
 *
 * <p>Cách tiếp cận này giúp:
 * <ul>
 *   <li>Giảm coupling giữa các controller</li>
 *   <li>Dễ mở rộng và bảo trì hệ thống điều hướng</li>
 *   <li>Tuân thủ nguyên lý Separation of Concerns (SoC)</li>
 * </ul>
 *
 * @param <T> kiểu dữ liệu đại diện cho view, route hoặc trạng thái điều hướng
 */
public abstract class PageController<T> {
  /**
   * Hàm xử lý chuyển đổi giao diện.
   */
  protected Consumer<T> switchView;


  /**
   * Thiết lập hành vi chuyển đổi giao diện cho controller.
   *
   * @param switchView hàm xử lý điều hướng sang view khác
   */
  public void setSwitchView(Consumer<T> switchView) {
    this.switchView = switchView;
  }
}
