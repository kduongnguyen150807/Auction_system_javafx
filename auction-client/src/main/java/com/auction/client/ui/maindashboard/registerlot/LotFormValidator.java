package com.auction.client.ui.maindashboard.registerlot;

import com.auction.client.ui.utils.ValidationResult;
import com.auction.client.ui.base.Validator;

/**
 * Chịu trách nhiệm kiểm tra tính hợp lệ của dữ liệu
 * trong form đăng ký vật phẩm đấu giá.
 *
 * <p>Class này tách riêng logic validation khỏi controller
 * nhằm:
 * <ul>
 *   <li>Tuân thủ nguyên lý Single Responsibility Principle (SRP)</li>
 *   <li>Giảm độ phức tạp cho controller</li>
 *   <li>Tăng khả năng tái sử dụng và kiểm thử</li>
 * </ul>
 *
 * <p>Các điều kiện được kiểm tra bao gồm:
 * <ul>
 *   <li>Tên sản phẩm</li>
 *   <li>Giá khởi điểm</li>
 *   <li>Giá mua ngay</li>
 *   <li>Thời gian bắt đầu và kết thúc</li>
 * </ul>
 */
public class LotFormValidator extends Validator<LotForm> {
  @Override
  public ValidationResult validate(LotForm form) {
    if (form.getName() == null || form.getName().length() < 5) {
      return ValidationResult.fail("Tên sản phẩm phải có ít nhất 5 ký tự!");
    }
    if (form.getStartPrice() <= 0) {
      return ValidationResult.fail("Giá khởi điểm phải lớn hơn 0!");
    }
    if (form.getBuyNowPrice() != null
      && form.getBuyNowPrice() < form.getStartPrice()) {

      return ValidationResult.fail(
        "Giá mua đứt phải lớn hơn giá ban đầu"
      );
    }
    if (form.getStartTime() == null || form.getEndTime() == null) {
      return ValidationResult.fail("Vui lòng chọn thời gian bắt đầu và kết thúc!");
    }
    if (form.getEndTime().isBefore(form.getStartTime())) {
      return ValidationResult.fail("Ngày kết thúc không thể trước ngày bắt đầu!");
    }
    return ValidationResult.ok();
  }
}