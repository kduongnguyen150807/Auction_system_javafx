package com.auction.shared;

import java.io.Serializable;

public abstract class Entity implements Serializable {
  private static final long serialVersionUID = 1L;
  protected int id;
  protected int version;

  public int getId() {
    int ans = this.id;
    return ans;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getVersion() {
    int ans = this.version;
    return ans;
  }

  public void setVersion(int v) {
    this.version = v;
  }
}
