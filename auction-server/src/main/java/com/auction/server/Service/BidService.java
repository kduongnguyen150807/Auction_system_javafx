package com.auction.server.Service;

import com.auction.server.dao.BidDao;
import com.auction.server.dao.ItemDao;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.Response;

import java.time.LocalDateTime;

public class BidService {
    private ItemDao itemDao;
    private BidDao bidDao;

    public BidService(){
        this.itemDao = ItemDao.getInstance();
        this.bidDao = BidDao.getInstance();
    }

    public Response placeBid(BidTransaction b){
        Response ans = null;
        Item i = this.itemDao.getById(b.getItemId());
        //check status cua item
        if (i == null || i.getStatus() != ItemStatus.OPEN) {
            ans = new Response("sys", Response.ERROR, "closed", null);
            return ans;
        }
        if (b.getBidValue() <= i.getCurrentPrice()) {
            ans = new Response("sys", Response.ERROR, "low", null);
            return ans;
        }
        //logic update bid_transaction va item
        boolean res = this.bidDao.placeBid(b);
        if(res){
            this.itemDao.updatePrice(i.getId(), b.getBidValue(), i.getVersion());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime end = i.getEndTime();
            if (now.plusSeconds(30).isAfter(end)){
                this.itemDao.updateEndtime(i.getId(), end.plusSeconds(60));
            }

            ans = new Response("sys", Response.OK, "success", b);
        }else{
            ans = new Response("sys", Response.ERROR, "conflict", null);
        }
        return ans;
    }
}
