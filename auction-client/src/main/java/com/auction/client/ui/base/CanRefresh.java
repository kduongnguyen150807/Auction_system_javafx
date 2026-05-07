package com.auction.client.ui.base;

/**
 * Định nghĩa hành vi cho các thành phần giao diện hoặc controller
 * có khả năng làm mới (refresh) dữ liệu hiển thị.
 *
 * <p>Các lớp triển khai interface này thường sử dụng phương thức
 * {@code refreshData()} để:
 * <ul>
 *   <li>Tải lại dữ liệu từ server hoặc database</li>
 *   <li>Cập nhật trạng thái giao diện sau khi dữ liệu thay đổi</li>
 *   <li>Đồng bộ dữ liệu hiển thị với trạng thái mới nhất</li>
 * </ul>
 */
public interface CanRefresh {
  /**
   * Làm mới dữ liệu và cập nhật lại giao diện hiển thị.
   */
  void refreshData();
}
