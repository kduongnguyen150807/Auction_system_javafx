package com.auction.client.ui.base;


/**
 * Định nghĩa contract cho các lớp thực hiện ánh xạ (mapping)
 * dữ liệu từ một kiểu nguồn sang một kiểu đích.
 *
 * <p>Interface này thường được sử dụng để:
 * <ul>
 *   <li>Chuyển đổi dữ liệu từ form UI sang model/domain object</li>
 *   <li>Chuyển đổi DTO sang entity và ngược lại</li>
 *   <li>Tách logic mapping khỏi controller nhằm tuân thủ nguyên lý
 *       Separation of Concerns (SoC)</li>
 * </ul>
 *
 * @param <F> kiểu dữ liệu nguồn (From)
 * @param <T> kiểu dữ liệu đích (Target)
 */
public interface Mapper<F, T> {

    /**
     * Thực hiện chuyển đổi dữ liệu từ kiểu nguồn sang kiểu đích.
     *
     * @param form dữ liệu nguồn cần ánh xạ
     * @return đối tượng sau khi được chuyển đổi
     */
    T map(F form);
}