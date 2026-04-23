package com.auction.server.controller;

import com.auction.server.service.TransactionService;
import com.auction.server.service.UserService;
import com.auction.server.service.WalletService;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.Map;
import java.util.Set;

public class UserRequestHandler implements RequestHandler {
    private final UserService userService;
    private final WalletService walletService;
    private final TransactionService transactionService;

    public UserRequestHandler(
            UserService userService,
            WalletService walletService,
            TransactionService transactionService) {
        this.userService = userService;
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    @Override
    public Set<String> supportedActions() {
        return Set.of(
                Request.UPDATE_PROFILE,
                Request.UPDATE_AVATAR,
                Request.GET_USER_BY_ID,
                Request.SEARCH_USERS,
                "refresh_user",
                "deposit",
                "get_transactions");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response handle(Request req, ClientHandler client) {
        String rid = req.getRequestId();
        String act = req.getAction();
        Object pay = req.getPayload();

        if (Request.UPDATE_PROFILE.equals(act)) {
            Map<String, String> data = (Map<String, String>) pay;
            String err =
                    userService.updateProfile(
                            Integer.parseInt(data.get("userid")),
                            data.get("fullname"),
                            data.get("email"),
                            data.get("phone"));
            return new Response(rid, err == null ? Response.OK : Response.ERROR, err == null ? "success" : err, null);
        }

        if (Request.UPDATE_AVATAR.equals(act)) {
            try {
                String[] data = ((String) pay).split(" ", 2);
                userService.updateAvatar(data[0], data[1]);
                return new Response(rid, Response.OK, "success", null);
            } catch (Exception e) {
                return new Response(rid, Response.ERROR, "fail", null);
            }
        }

        if (Request.GET_USER_BY_ID.equals(act)) {
            User u = userService.getUserById((int) pay);
            if (u != null) u.setPassword("");
            return new Response(rid, u != null ? Response.OK : Response.ERROR, u != null ? "success" : "not_found", u);
        }

        if (Request.SEARCH_USERS.equals(act)) {
            return new Response(rid, Response.OK, "success", (java.io.Serializable) userService.searchUsers((String) pay));
        }

        if ("refresh_user".equals(act)) {
            User u = userService.getUserById((int) pay);
            return new Response(rid, u != null ? Response.OK : Response.ERROR, u != null ? "success" : "fail", u);
        }

        if ("deposit".equals(act)) {
            Map<String, String> data = (Map<String, String>) pay;
            int userId = Integer.parseInt(data.get("userid"));
            double amount = Double.parseDouble(data.get("amount"));
            User u = walletService.deposit(userId, amount);
            return new Response(rid, u != null ? Response.OK : Response.ERROR, u != null ? "success" : "fail", u);
        }

        if ("get_transactions".equals(act)) {
            int userId = (int) pay;
            return new Response(rid, Response.OK, "success",
                    (java.io.Serializable) transactionService.getTransactions(userId));
        }

        return new Response(rid, Response.ERROR, "unknown_action", null);
    }
}