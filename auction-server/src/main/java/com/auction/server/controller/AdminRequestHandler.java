package com.auction.server.controller;

import com.auction.server.service.AdminService;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.UserRole;
import java.util.Set;

public class AdminRequestHandler implements RequestHandler {
    private final AdminService adminService;

    public AdminRequestHandler(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    public Set<String> supportedActions() {
        return Set.of(
                Request.GET_ALL_USERS,
                Request.LOCK_USER,
                Request.UNLOCK_USER,
                Request.PROMOTE_ADMIN,
                Request.GET_PENDING_ITEMS,
                Request.APPROVE_ITEM,
                Request.REJECT_ITEM,
                "get_status_stats",
                "get_category_stats");
    }

    @Override
    public Response handle(Request req, ClientHandler client) {
        String rid = req.getRequestId();

        if (!adminService.isAdmin(client.getCurrentUser())) {
            return new Response(rid, Response.ERROR, "forbidden", null);
        }

        String act = req.getAction();
        Object pay = req.getPayload();

        if (Request.GET_ALL_USERS.equals(act)) {
            return new Response(rid, Response.OK, "success",
                    (java.io.Serializable) adminService.getAllUsers());
        }

        if (Request.LOCK_USER.equals(act)) {
            boolean ok = adminService.lockUser((String) pay);
            return new Response(rid, ok ? Response.OK : Response.ERROR, ok ? "success" : "fail", null);
        }

        if (Request.UNLOCK_USER.equals(act)) {
            boolean ok = adminService.unlockUser((String) pay);
            return new Response(rid, ok ? Response.OK : Response.ERROR, ok ? "success" : "fail", null);
        }

        if (Request.PROMOTE_ADMIN.equals(act)) {
            String raw = (String) pay;
            String[] parts = raw.split(":");
            String username = parts[0];
            String role = parts.length > 1 ? parts[1] : UserRole.ADMIN.name();
            boolean ok = adminService.setUserRole(username, role);
            return new Response(rid, ok ? Response.OK : Response.ERROR, ok ? "success" : "fail", null);
        }

        if (Request.GET_PENDING_ITEMS.equals(act)) {
            return new Response(rid, Response.OK, "success",
                    (java.io.Serializable) adminService.getPendingItems());
        }

        if (Request.APPROVE_ITEM.equals(act)) {
            boolean ok = adminService.approveItem((int) pay);
            return new Response(rid, ok ? Response.OK : Response.ERROR, ok ? "success" : "fail", null);
        }

        if (Request.REJECT_ITEM.equals(act)) {
            boolean ok = adminService.rejectItem((int) pay);
            return new Response(rid, ok ? Response.OK : Response.ERROR, ok ? "success" : "fail", null);
        }

        if ("get_status_stats".equals(act)) {
            return new Response(rid, Response.OK, "success", (java.io.Serializable) adminService.getStatusStats());
        }

        if ("get_category_stats".equals(act)) {
            return new Response(rid, Response.OK, "success", (java.io.Serializable) adminService.getCategoryStats());
        }

        return new Response(rid, Response.ERROR, "unknown_action", null);
    }
}