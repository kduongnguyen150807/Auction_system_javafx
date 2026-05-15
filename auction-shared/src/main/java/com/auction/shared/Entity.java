package com.auction.shared;

import java.io.Serializable;

public abstract class Entity implements Serializable {
  private static final long serialVersionUID = 1L;
  protected int id;
  // Tính năng 3: Optimistic Locking version
  protected int version;

  public int getId() { return id; }
  public void setId(int id) { this.id = id; }

  public int getVersion() { return version; }
  public void setVersion(int version) { this.version = version; }
}