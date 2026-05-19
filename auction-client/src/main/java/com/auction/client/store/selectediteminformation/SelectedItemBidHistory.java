package com.auction.client.store.selectediteminformation;

import com.auction.client.util.FXThread;
import com.auction.shared.BidTransaction;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class SelectedItemBidHistory {
  public static SelectedItemBidHistory SELECTED_ITEM_BID_HISTORY = new SelectedItemBidHistory();

  private final IntegerProperty selectedItemId = new SimpleIntegerProperty(-1);

  private final ObservableList<BidTransaction> selectedItemBidHistory = FXCollections.observableArrayList();

  public static int MAX_POINTS = 20;

  private SelectedItemBidHistory() {}

  public int getSelectedItemId() {
    return selectedItemId.get();
  }

  public IntegerProperty selectedItemIdProperty() {
    return selectedItemId;
  }

  public ObservableList<BidTransaction> selectedItemBidHistoryProperty() {
    return selectedItemBidHistory;
  }

  public void setSelectedItem(int itemId, List<BidTransaction> history) {
    clear();
    selectedItemId.set(itemId);

    List<BidTransaction> copy =
      history != null ? new ArrayList<>(history) : List.of();

    FXThread.run(() -> selectedItemBidHistory.setAll(copy));
  }

  public void appendBidTransaction(BidTransaction bid) {
    if (bid == null) {
      return;
    }

    if ( bid.getItemId() != getSelectedItemId()) {
      return;
    }

    FXThread.run(() -> {
      selectedItemBidHistory.add(bid);
      if (selectedItemBidHistory.size() > MAX_POINTS) {
        selectedItemBidHistory.removeFirst();
      }
    });
  }

  public void clear() {
    selectedItemId.set(-1);
    FXThread.run(selectedItemBidHistory::clear);
  }
}
