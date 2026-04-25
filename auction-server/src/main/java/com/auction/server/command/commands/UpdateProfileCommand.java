package com.auction.server.command.commands;

import com.auction.server.command.Command;
import com.auction.server.dao.UserDao;
import com.auction.shared.Response;
import com.auction.shared.User.User;

import java.util.Map;

public class UpdateProfileCommand implements Command {
    @Override
    public Response execute(Object data, String requestId, User u){
        Map<String, String> UserData = (Map<String, String>) data;
        boolean res = UserDao.getInstance().updateProfile(
                Integer.parseInt(UserData.get("userid")),
                UserData.get("fullname"),
                UserData.get("email"),
                UserData.get("phone"));
        return new Response(requestId, res ? Response.OK : Response.ERROR, res ? "success" : "fail", null);
    }
}
