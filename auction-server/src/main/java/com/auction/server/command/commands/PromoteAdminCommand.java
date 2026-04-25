package com.auction.server.command.commands;

import com.auction.server.command.Authorizable;
import com.auction.server.command.Command;
import com.auction.server.dao.UserDao;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User.User;
import com.auction.shared.UserRole;

public class PromoteAdminCommand implements Command, Authorizable {
    @Override
    public Response execute(Object data, String requestId, User u){
        if(isAdmin(u)){
            String[] data1 = ((String) data).split(":");
            String role = data1.length > 1 ? data1[1] : UserRole.ADMIN.name();
            boolean res = UserDao.getInstance().setUserRole(data1[0], role);
            return new Response(requestId, res ? Response.OK: Response.ERROR, res ? "success" : "fail", null);
        }else {
            return new Response(requestId, Response.ERROR, "ur not admin nigga", null);
        }
    }
}
