package com.auction.client.store.lotsinformation;

import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import javafx.beans.property.*;

import java.time.LocalDateTime;

public class ItemModel {
  private final LongProperty id = new SimpleLongProperty();
  private final StringProperty name = new SimpleStringProperty();
  private final StringProperty description = new SimpleStringProperty();
  private final DoubleProperty currentPrice = new SimpleDoubleProperty();
  private final ObjectProperty<ItemStatus> status = new SimpleObjectProperty<>();
  private final ObjectProperty<LocalDateTime> endTime = new SimpleObjectProperty<>();

  private Item item;

  private final String category;

  public ItemModel(Item item) {
    id.set(item.getId());
    name.set(item.getName());
    currentPrice.set(item.getCurrentPrice());
    status.set(item.getStatus());
    description.set(item.getDescription());
    endTime.set(item.getEndTime());

    this.category = item.getCategory();
    this.item = item;
  }

  public void update(Item item) {
    currentPrice.set(item.getCurrentPrice());
    status.set(item.getStatus());
  }

  public Item getItem() {
    return item;
  }

  public String getCategory() {
    return category;
  }

  public ObjectProperty<LocalDateTime> endTimeProperty() {
    return endTime;
  }

  public StringProperty descriptionProperty() {
    return description;
  }

  public int getId() {
    return (int) id.get();
  }

  public LongProperty idProperty() {
    return id;
  }

  public StringProperty nameProperty() {
    return name;
  }

  public DoubleProperty currentPriceProperty() {
    return currentPrice;
  }

  public ItemStatus getStatus() {
    return status.get();
  }

  public ObjectProperty<ItemStatus> statusProperty() {
    return status;
  }

  public void setStatus(ItemStatus status) {
    this.status.set(status);
  }

  public void setEndTime(LocalDateTime endTime) {
    this.endTime.set(endTime);
  }

  public void setCurrentPrice(double currentPrice) {
    this.currentPrice.set(currentPrice);
  }

  public void setName(String name) {
    this.name.set(name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ItemModel that = (ItemModel) o;
    return this.getItem() != null && that.getItem() != null &&
      this.getItem().getId() == that.getItem().getId();
  }

  @Override
  public int hashCode() {
    return this.getItem() != null ? java.util.Objects.hash(this.getItem().getId()) : super.hashCode();
  }
}
