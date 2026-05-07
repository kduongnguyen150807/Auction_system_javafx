package com.auction.client.ui.maindashboard.registerlot;

import com.auction.client.ClientSession;
import com.auction.client.ui.base.Mapper;
import com.auction.shared.item.Item;
import com.auction.shared.item.ItemFactory;

import java.util.Objects;

/**
 * Chịu trách nhiệm chuyển đổi dữ liệu từ {@link LotForm}
 * sang domain model {@link Item}.
 *
 * <p>Class này giúp tách riêng logic mapping khỏi controller,
 * từ đó:
 * <ul>
 *   <li>Giảm coupling giữa UI và domain layer</li>
 *   <li>Tuân thủ nguyên lý Separation of Concerns (SoC)</li>
 *   <li>Tăng khả năng tái sử dụng và kiểm thử</li>
 * </ul>
 *
 * <p>Việc tạo đối tượng {@link Item} được thực hiện thông qua
 * {@link ItemFactory} nhằm đảm bảo loại vật phẩm phù hợp
 * với category được chọn trong form.
 */
public class LotMapper implements Mapper<LotForm, Item> {

    /**
     * Chuyển đổi dữ liệu từ form đăng ký vật phẩm
     * sang domain model {@link Item}.
     *
     * @param form dữ liệu form cần chuyển đổi
     * @return đối tượng {@link Item} tương ứng,
     *         hoặc {@code null} nếu form null
     */
    @Override
    public Item map(LotForm form) {

        Objects.requireNonNull(form);

        Objects.requireNonNull(
          form.getCategory()
        );

        Item item =
          ItemFactory.createItem(
            form.getCategory()
          );

        item.setName(form.getName());
        item.setDescription(form.getDescription());
        item.setStartTime(form.getStartTime());
        item.setEndTime(form.getEndTime());
        item.setStartingPrice(form.getStartPrice());
        item.setMaxPrice(form.getBuyNowPrice());

        item.setImageUrl(
          Objects.requireNonNullElse(
            form.getImageUrl(),
            ""
          )
        );

        return item;
    }
}