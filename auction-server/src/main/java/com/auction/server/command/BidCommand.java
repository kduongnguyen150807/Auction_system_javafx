package com.auction.server.command;

import com.auction.server.Service.AuctionManager;
import com.auction.shared.BidTransaction;
import com.auction.shared.Response;
import com.auction.shared.User;

public class BidCommand implements Command{
    @Override
    public Response execute(Object data, String requestId, User u){
        BidTransaction bidTransaction = (BidTransaction) data;
        Response res = AuctionManager.getInstance().processBid(bidTransaction);
        if(res == null){
            return new Response(requestId, null, null, null);
        }
        return new Response(requestId, res.getStatus(), res.getMessage(), res.getPayload());
    }
}
