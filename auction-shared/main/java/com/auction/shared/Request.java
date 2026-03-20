package com.auction.shared;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
public class Request implements Serializable {
    public static final String login = "LOGIN";
    public static final String signup = "SIGNUP";
    public static final String bid = "BID";
    public static final String add = "ADD";
    public static final String list = "LIST";
    private String id;
    private String act;
    private Object obj;
    private LocalDateTime ts;
    public Request(String act, Object obj) {
        this.id = UUID.randomUUID().toString();
        this.act = act;
        this.obj = obj;
        this.ts = LocalDateTime.now();
    }
    public String getid() {
        String ans = this.id;
        return ans;
    }
    public String getact() {
        String ans = this.act;
        return ans;
    }
    public Object getobj() {
        Object ans = this.obj;
        return ans;
    }
    public LocalDateTime getts() {
        LocalDateTime ans = this.ts;
        return ans;
    }
}