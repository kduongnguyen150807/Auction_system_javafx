package com.auction.shared;

import java.io.Serializable;

public abstract class Entity implements Serializable {
  private static final long serialVersionUID = 1L;
  protected int id;
  protected int version;

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getVersion() {
    return this.version;
  }

  public void setVersion(int version) {
    this.version = version;
  }
}
