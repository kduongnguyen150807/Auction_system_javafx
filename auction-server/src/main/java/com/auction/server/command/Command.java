package com.auction.server.command;

import com.auction.shared.Response;
import com.auction.shared.User.User;

public interface Command {
    Response execute(Object data, String rid, User u);
}
