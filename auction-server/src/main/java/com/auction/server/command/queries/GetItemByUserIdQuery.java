package com.auction.server.command.queries;

import com.auction.server.command.Command;
import com.auction.shared.Response;
import com.auction.shared.User.User;

public class GetItemByUserIdQuery implements Command {
    @Override
    public Response execute(Object data, String requestId, User u){
        return null;
    }
}
