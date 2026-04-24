package com.auction.server.command.queries;

import com.auction.server.command.Command;
import com.auction.server.dao.ItemDao;
import com.auction.shared.*;
import com.auction.shared.Item.Item;
import com.auction.shared.Item.ItemStatus;
import com.auction.shared.User.User;

import java.util.List;

public class GetPendingItemsQuery implements Command {
    @Override
    public Response execute(Object data, String rid, User user){
        Response ans = null;
        List<Item> pendingItems = ItemDao.getInstance().getItemByStatus(ItemStatus.PENDING);
        ans = new Response(rid, Response.OK, "success", (java.io.Serializable) pendingItems);
        return ans;
    }
}
