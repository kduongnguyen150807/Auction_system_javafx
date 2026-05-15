package com.auction.client.ui.TrangChu;

import com.auction.shared.Item;
import java.util.List;

/** Combined home catalog payload (full ongoing list + trending top-N from server). */
public record TrangChuCatalogLoadResult(List<Item> ongoing, List<Item> trending) {}
