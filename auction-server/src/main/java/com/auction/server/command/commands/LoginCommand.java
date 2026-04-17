package com.auction.server.command.commands;

import com.auction.server.command.Command;
import com.auction.server.dao.UserDao;
import com.auction.shared.*;

import java.util.Map;

public class LoginCommand implements Command {
    @Override
    public Response execute(Object data,String rid, User u) {
        if(!(data instanceof Map)){
            return new Response(rid, Response.ERROR, "fail", null);
        }

        Map<String, String> credentials = (Map<String, String>) data;
        String username = credentials.get("username");
        String password = credentials.get("password");

        User user = UserDao.getInstance().login(username, password);

        if (user != null) {
            System.out.println("User " + username + " logged in successfully.");
            return new Response(rid, Response.OK, "success", user);
        } else {
            return new Response(rid, Response.ERROR, "fail", null);
        }
    }
}
