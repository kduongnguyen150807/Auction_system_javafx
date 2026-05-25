package com.auction.client.store.clientinformation;

import com.auction.client.util.FXThread;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.Collection;

public class IdStateManager<T> {
  private final ObservableSet<T> idSet = FXCollections.observableSet();

  public void initialize(Collection<T> ids) {
    FXThread.run(() -> {
      this.idSet.clear();
      if (ids != null) {
        this.idSet.addAll(ids);
      }
    });
  }

  public ObservableSet<T> getIdSet() {
    return idSet;
  }

  public boolean contain(T id) {
    return idSet.contains(id);
  }

  public void toggle(T id, boolean exist) {
    FXThread.run(() -> {
      if (exist) idSet.add(id);
      else idSet.remove(id);
    });
  }
}
