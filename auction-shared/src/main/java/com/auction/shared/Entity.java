package com.auction.shared;
import java.io.Serializable;
public abstract class Entity implements Serializable {
    protected int id;
    protected int version;
    public int getid() {
        int ans = this.id;
        return ans;
    }
    public void setid(int id) {
        this.id = id;
    }
    public int getversion() {
        int ans = this.version;
        return ans;
    }
    public void setversion(int v) {
        this.version = v;
    }
}