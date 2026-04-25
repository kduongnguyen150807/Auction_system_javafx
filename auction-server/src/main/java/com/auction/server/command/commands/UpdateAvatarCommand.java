package com.auction.server.command.commands;

import com.auction.server.command.Command;
import com.auction.server.dao.UserDao;
import com.auction.shared.Response;
import com.auction.shared.User.User;

public class UpdateAvatarCommand implements Command {
    @Override
    public Response execute(Object data, String requestId, User u){
        String[] dt = ((String) data).split(" ");
        boolean res = UserDao.getInstance().updateAvatar(dt[0], dt[1]);
        return new Response(requestId, res ? Response.OK: Response.ERROR, res ? "success": "fail", null);
    }
}
