package com.auction.server.command.commands;

import com.auction.server.command.Authorizable;
import com.auction.server.command.Command;
import com.auction.server.dao.ItemDao;
import com.auction.shared.Response;
import com.auction.shared.User.User;

public class ApproveItemCommand implements Command, Authorizable {
    @Override
    public Response execute(Object data, String requestId, User user){
        Response response = null;
        if(isAdmin(user)){
            boolean res = ItemDao.getInstance().approveItem((int) data);
            response = new Response(requestId, res ? Response.OK: Response.ERROR, res ? "success" : "fail", null);
        }else {
            response = new Response(requestId, Response.ERROR, "forbidden", null);
        }
        return response;
    }
}
