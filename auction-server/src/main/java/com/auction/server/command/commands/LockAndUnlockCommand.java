package com.auction.server.command.commands;

import com.auction.server.command.Authorizable;
import com.auction.server.command.Command;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.UserDao;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User.User;

import java.util.concurrent.locks.ReentrantLock;

public class LockAndUnlockCommand implements Command, Authorizable {
    @Override
    public Response execute(Object data, String requestId, User u){
        Response response;
        if(isAdmin(u)){
            boolean lock = data.equals(Request.LOCK_USER);
            boolean res = UserDao.getInstance().setUserLocked(u.getUsername(), lock);
            response = new Response(requestId, res ? Response.OK: Response.ERROR, res ? "success" : "fail", null);
        }else {
            response = new Response(requestId, Response.ERROR, "ur not admin nigga", null);
        }
        return response;
    }
}
