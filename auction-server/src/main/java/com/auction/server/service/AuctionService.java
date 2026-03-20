<<<<<<< HEAD
<<<<<<< HEAD
package com.auction.server.service;
import com.auction.server.dao.*;
import com.auction.shared.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
public class AuctionService {
    private ItemDao idao = new ItemDao();
    private BidDao bdao = new BidDao();
    public synchronized String placebid(int iid, int uid, double val) {
        String res = "error";
        Item i = idao.getbyid(iid);
        if (i == null) return "not_found";
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(i.getendtime())) return "ended";
        if (val <= i.getcurrentprice()) return "low_price";
        if (idao.updateprice(iid, val)) {
            BidTransaction b = new BidTransaction(iid, uid, val);
            b.setts(now);
            bdao.addbid(b);
            if (ChronoUnit.SECONDS.between(now, i.getendtime()) <= 30) {
                LocalDateTime newend = i.getendtime().plusSeconds(30);
                idao.updateendtime(iid, newend);
            }
            res = "success";
        }
        return res;
    }
}
=======

>>>>>>> ac326420b33c0c98046ce5c55e9215f10777f773
=======

>>>>>>> ac326420b33c0c98046ce5c55e9215f10777f773
