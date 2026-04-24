package com.auction.server.command.queries;

import com.auction.server.command.Command;
import com.auction.server.dao.UserDao;
import com.auction.shared.*;
import com.auction.shared.User.User;

import java.util.List;

public class GetAllUserQuery implements Command {
    @Override
    public Response execute(Object data, String requestId, User u){
        List<User> userList = UserDao.getInstance().getAllUser();
        System.out.println("get all user completed");
        return new Response(requestId, Response.OK, "success", userList);
    }
}
