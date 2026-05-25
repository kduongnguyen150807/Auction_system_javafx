package com.auction.client.store.lotsinformation;

import com.auction.client.util.FXThread;
import com.auction.shared.Item;
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

        if (ACTIVE_POOL.containsKey(item.getId())) {
            return ACTIVE_POOL.get(item.getId());
        } else {
            ItemModel itemModel = new ItemModel(item);
            FXThread.run(() -> ACTIVE_POOL.put(item.getId(), itemModel));
            return itemModel;
        }
    }

    public static void updateIfNewer(Item newItem) {
        if (newItem == null) return;

        if (ACTIVE_POOL.containsKey(newItem.getId())) {
            ItemModel itemModel = ACTIVE_POOL.get(newItem.getId());
            itemModel.update(newItem);
        } else {
            getOrCreate(newItem);
        }
    }

    public static void removeItem(Item item) {
        if (item == null) return;
        if (ACTIVE_POOL.containsKey(item.getId())) {
            FXThread.run(() -> ACTIVE_POOL.remove(item.getId()));
        }
    }
}