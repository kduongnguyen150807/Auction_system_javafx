package com.auction.client.store.lotsinformation;

import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ItemModelRegistry {

    private static final Map<Integer, ItemModel> ACTIVE_POOL = new ConcurrentHashMap<>();

    private ItemModelRegistry() {
    }

    public static ItemModel getOrCreate(Item item) {
        if (item == null) {
            return null;
        }

        if (item.getStatus() == ItemStatus.CLOSED) {
            ItemModel closedModel = ACTIVE_POOL.remove(item.getId());
            if (closedModel != null) {
                closedModel.update(item);
                return closedModel;
            }
            return new ItemModel(item);
        }

        return ACTIVE_POOL.computeIfAbsent(item.getId(), id -> new ItemModel(item));
    }

    public static void updateIfNewer(Item newItem) {
        if (newItem == null) return;

        ACTIVE_POOL.computeIfPresent(newItem.getId(), (id, existingModel) -> {
            Item currentItem = existingModel.getItem();
            if (newItem.getCurrentPrice() <= currentItem.getCurrentPrice()
              && newItem.getStatus() == currentItem.getStatus()) {
                return existingModel;
            }

            existingModel.update(newItem);

            if (newItem.getStatus() == ItemStatus.CLOSED) {
                return null;
            }

            return existingModel;
        });
    }
}