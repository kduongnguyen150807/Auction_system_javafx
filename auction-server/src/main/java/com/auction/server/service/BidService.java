package com.auction.server.service;
import com.auction.shared.*;
import com.auction.server.dao.*;
import java.time.LocalDateTime;
public class BidService {
    private ItemDao itemdao;
    private BidDao biddao;
    public BidService() {
        this.itemdao = new ItemDao();
        this.biddao = new BidDao();
    }
    public Response placebid(BidTransaction b) {
        Response ans = null;
        Item i = this.itemdao.getbyid(b.getitemid());
        if (i == null || i.getstatus() != ItemStatus.OPEN) {
            ans = new Response("sys", Response.err, "closed", null);
            return ans;
        }
        if (b.getbidvalue() <= i.getcurrentprice()) {
            ans = new Response("sys", Response.err, "low", null);
            return ans;
        }
        boolean up = this.itemdao.updateprice(i.getid(), b.getbidvalue(), i.getversion());
        if (up) {
            this.biddao.addbid(b);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime end = i.getendtime();
            if (now.plusSeconds(30).isAfter(end)) {
                this.itemdao.updateendtime(i.getid(), end.plusSeconds(60));
            }
            ans = new Response("sys", Response.ok, "success", b);
        } else {
            ans = new Response("sys", Response.err, "conflict", null);
        }
        return ans;
    }
}