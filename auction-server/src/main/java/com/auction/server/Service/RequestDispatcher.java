package com.auction.server.Service;

import com.auction.server.command.*;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;

import java.util.HashMap;
import java.util.Map;

public class RequestDispatcher {
    private static final Map<String, Command> commands = new HashMap<>();
    static {
        commands.put(Request.LOGIN, new LoginCommand());
        commands.put(Request.ADD_LOT, new AddLotCommand());
        commands.put(Request.GET_PENDING_ITEMS, new GetPendingItemsCommand());
        commands.put(Request.APPROVE_ITEM, new ApproveItemCommand());
        commands.put(Request.GET_ONGOING_BIDS, new GetOnGoingBindCommand());
    }

    public static Response dispatch(Request req, User u){
        Command cmd = commands.get(req.getAction());
        if(cmd!=null){
            return cmd.execute(req.getPayload(), req.getRequestId(), u);
        }
        return null;
    }
}
