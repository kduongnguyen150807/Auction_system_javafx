package com.auction.server.command.queries;

import com.auction.server.command.Command;
import com.auction.server.dao.BidDao;
import com.auction.shared.BidTransaction;
import com.auction.shared.Response;
import com.auction.shared.User;

import java.util.List;

public class GetBidHistoryQuery implements Command {
    @Override
    public Response execute(Object data, String requestId, User u){
        int itemId = (int) data;
        List<BidTransaction> bidHistory = BidDao.getInstance().GetBidHistory(itemId);
        return new Response(requestId, Response.OK, "success", (java.io.Serializable) bidHistory);
    }
}
