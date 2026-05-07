package com.auction.client.ui.base;

import com.auction.client.ui.utils.ValidationResult;

/**
 * Lớp cơ sở định nghĩa cơ chế kiểm tra tính hợp lệ của dữ liệu.
 *
 * <p>Các lớp kế thừa {@code Validator} sẽ triển khai logic kiểm tra
 * cho từng loại dữ liệu cụ thể như:
 * <ul>
 *   <li>Form tạo sản phẩm đấu giá</li>
 * </ul>
 *
 * <p>Việc tách riêng validation khỏi controller giúp:
 * <ul>
 *   <li>Tuân thủ nguyên lý Single Responsibility Principle (SRP)</li>
 *   <li>Giảm độ phức tạp của controller</li>
 *   <li>Dễ kiểm thử và tái sử dụng logic validation</li>
 * </ul>
 *
 * @param <T> kiểu dữ liệu cần kiểm tra
 */
public abstract class Validator<T> {
  /**
   * Kiểm tra tính hợp lệ của dữ liệu đầu vào.
   *
   * @param t dữ liệu cần kiểm tra
   * @return kết quả validation bao gồm trạng thái hợp lệ
   *         và thông báo lỗi nếu có
   */
  protected abstract ValidationResult validate(T t);
}
