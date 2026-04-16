package com.auction.server.command;

import com.auction.server.dao.UserDao;
import com.auction.shared.*;

import java.util.List;

public class GetAllUserCommand implements Command{
    @Override
    public Response execute(Object data, String requestId, User u){
        List<User> userList = UserDao.getInstance().getAllUser();
        System.out.println("get all user completed");
        return new Response(requestId, Response.OK, "success", userList);
    }
}
