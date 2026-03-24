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
    protected String requestid;
    protected String action;
    protected Object payload;
    protected LocalDateTime timestamp;
    public Request(String act, Object obj) {
        this.requestid = UUID.randomUUID().toString();
        this.action = act;
        this.payload = obj;
        this.timestamp = LocalDateTime.now();
    }
    public String getrequestid() { String ans = this.requestid; return ans; }
    public String getaction() { String ans = this.action; return ans; }
    public Object getpayload() { Object ans = this.payload; return ans; }
    public LocalDateTime gettimestamp() { LocalDateTime ans = this.timestamp; return ans; }
}