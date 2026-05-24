package com.auction.client.store.clientinformation;

import com.auction.client.util.FXThread;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.Collection;

public class IdStateManager {
  private final ObservableSet<Integer> idSet = FXCollections.observableSet();

  public void initialize(Collection<Integer> ids) {
    FXThread.run(() -> {
      this.idSet.clear();
      if (ids != null) {
        this.idSet.addAll(ids);
      }
    });
  }

  public ObservableSet<Integer> getIdSet() {
    return idSet;
  }

  public boolean contain(int id) {
    return idSet.contains(id);
  }

  public void toggle(int id, boolean exist) {
    FXThread.run(() -> {
      if (exist) idSet.add(id);
      else idSet.remove(id);
    });
  }
}
