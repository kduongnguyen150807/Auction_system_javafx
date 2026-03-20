package com.auction.shared;
import java.io.Serializable;
import java.time.LocalDateTime;
public class Response implements Serializable {
    public static final String ok = "SUCCESS";
    public static final String err = "ERROR";
    private String rid;
    private String st;
    private String msg;
    private Object obj;
    private LocalDateTime ts;
    public Response(String rid, String st, String msg, Object obj) {
        this.rid = rid;
        this.st = st;
        this.msg = msg;
        this.obj = obj;
        this.ts = LocalDateTime.now();
    }
    public String getrid() { return rid; }
    public String getst() { return st; }
    public String getmsg() { return msg; }
    public Object getobj() { return obj; }
    public LocalDateTime getts() { return ts; }
}