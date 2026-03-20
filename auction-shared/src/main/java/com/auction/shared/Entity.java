package com.auction.shared;
import java.io.Serializable;
public abstract class Entity implements Serializable {
    protected int id;
    public int getid() {
        int ans = this.id;
        return ans;
    }
    public void setid(int id) {
        this.id = id;
    }
}