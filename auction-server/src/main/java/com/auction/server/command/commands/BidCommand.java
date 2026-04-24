package com.auction.server.command.commands;

import com.auction.server.Service.AuctionManager;
import com.auction.server.command.Authorizable;
import com.auction.server.command.Command;
import com.auction.shared.BidTransaction;
import com.auction.shared.Response;
import com.auction.shared.User.User;

public class BidCommand implements Command, Authorizable {
    @Override
    public Response execute(Object data, String requestId, User u){
        BidTransaction bidTransaction = (BidTransaction) data;
        if(!isOwner(u, bidTransaction.getUserId())){
            return new Response(requestId, Response.ERROR, "yo your not real nigger", null);
        }
        Response res = AuctionManager.getInstance().processBid(bidTransaction);
        System.out.println("returning");
        System.out.println(res.getMessage());
        if(res == null){
            return new Response(requestId, null, null, null);
        }
        return new Response(requestId, res.getStatus(), res.getMessage(), res.getPayload());
    }
}
