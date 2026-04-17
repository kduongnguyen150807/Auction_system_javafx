package com.auction.server.command.commands;

import com.auction.server.command.Command;
import com.auction.server.dao.UserDao;
import com.auction.shared.Response;
import com.auction.shared.User;

public class SignUpCommand implements Command {
    @Override
    public Response execute(Object data, String requestId, User u){
        try {
            User res = (User) data;
            boolean res2 = UserDao.getInstance().signup(res);
            return  new Response(requestId, res2 ? Response.OK : Response.ERROR, res2 ? "success" : "duplicate", null);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return new Response(requestId, Response.ERROR, "class_cast_error", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(requestId, Response.ERROR, "server_error", null);
        }
    }
}
