package com.auction.shared;
import java.io.Serializable;
import java.time.LocalDateTime;
public class Response implements Serializable {
    public static final String ok = "SUCCESS";
    public static final String err = "ERROR";
    protected String requestid;
    protected String status;
    protected String message;
    protected Object payload;
    protected LocalDateTime timestamp;
    public Response(String rid, String st, String msg, Object obj) {
        this.requestid = rid;
        this.status = st;
        this.message = msg;
        this.payload = obj;
        this.timestamp = LocalDateTime.now();
    }
    public String getrequestid() { String ans = this.requestid; return ans; }
    public String getstatus() { String ans = this.status; return ans; }
    public String getmessage() { String ans = this.message; return ans; }
    public Object getpayload() { Object ans = this.payload; return ans; }
    public LocalDateTime gettimestamp() { LocalDateTime ans = this.timestamp; return ans; }
}