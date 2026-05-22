package com.auction.server.handler.user;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.VipPlan;

public class GetVipPlansHandler implements ActionHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    return new Response(request.getRequestId(), Response.OK, "success", VipPlan.allPlans());
  }
}
