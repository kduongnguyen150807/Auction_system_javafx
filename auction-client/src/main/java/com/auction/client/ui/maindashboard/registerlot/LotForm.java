package com.auction.client.ui.maindashboard.registerlot;

import com.auction.shared.item.ItemType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Đại diện cho dữ liệu của form đăng ký vật phẩm đấu giá.
 *
 * <p>{@code LotForm} đóng vai trò là lớp trung gian giữa
 * giao diện người dùng (UI) và domain model.
 *
 * <p>Class này được sử dụng để:
 * <ul>
 *   <li>Thu thập dữ liệu nhập từ form JavaFX</li>
 *   <li>Truyền dữ liệu sang tầng validation</li>
 *   <li>Chuyển đổi dữ liệu sang model {@code Item}</li>
 * </ul>
 *
 * <p>Việc tách riêng form object giúp:
 * <ul>
 *   <li>Giảm coupling giữa UI và domain model</li>
 *   <li>Dễ validate dữ liệu đầu vào</li>
 *   <li>Tuân thủ nguyên lý Separation of Concerns (SoC)</li>
 * </ul>
 */
public class LotForm {
  private String name;
  private double startPrice;
  private Double buyNowPrice; // Dùng Double thay vì double để cho phép null
  private String description;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private ItemType category;
  private String imageUrl;

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getStartPrice() {
    return startPrice;
  }

  public void setStartPrice(double startPrice) {
    this.startPrice = startPrice;
  }

  public Double getBuyNowPrice() {
    return buyNowPrice;
  }

  public void setBuyNowPrice(Double buyNowPrice) {
    this.buyNowPrice = buyNowPrice;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  public ItemType getCategory() {
    return category;
  }

  public void setCategory(ItemType category) {
    this.category = category;
  }
}
