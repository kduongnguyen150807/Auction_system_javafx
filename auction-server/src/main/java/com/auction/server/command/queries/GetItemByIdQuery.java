package com.auction.server.command.queries;

import com.auction.server.command.Command;
import com.auction.server.dao.ItemDao;
import com.auction.shared.Item;
import com.auction.shared.Response;
import com.auction.shared.User;

public class GetItemByIdQuery implements Command {
    @Override
    public Response execute(Object data, String requestId, User u){
        Item item = ItemDao.getInstance().getById((int) data);
        return new Response(requestId, item != null ? Response.OK : Response.ERROR,
                item != null ? "success" : "not_found",
                item);
    }
}
