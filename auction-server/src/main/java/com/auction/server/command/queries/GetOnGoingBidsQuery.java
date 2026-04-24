package com.auction.server.command.queries;

import com.auction.server.command.Command;
import com.auction.server.dao.ItemDao;
import com.auction.shared.Item.Item;
import com.auction.shared.Item.ItemStatus;
import com.auction.shared.Response;
import com.auction.shared.User.User;

import java.util.List;

public class GetOnGoingBidsQuery implements Command {
    @Override
    public Response execute(Object data, String rid, User u) {
        Response ans = null;
        List<Item> ongoingItem = ItemDao.getInstance().getItemByStatus(ItemStatus.OPEN);
        ans = new Response(rid, Response.OK, "success", (java.io.Serializable) ongoingItem);
        return ans;
    }
}
