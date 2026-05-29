package com.auction.server.handler.chat;

import com.auction.server.dao.chat.FriendDao;
import com.auction.server.dao.user.UserDao;
import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.service.auction.AuctionManager;
import com.auction.shared.Friendship;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.util.List;

public class FriendHandler implements ActionHandler {
  private final FriendDao friendDao = new FriendDao();
  private final UserDao userDao = new UserDao();

  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String rid = request.getRequestId();
    User me = context.getCurrentUser();

    if (me == null) {
      return new Response(rid, Response.ERROR, "not_logged_in", null);
    }

    switch (action) {
      case Request.ADD_FRIEND: {
        int targetId = ((Number) request.getPayload()).intValue();

        if (targetId == me.getId()) {
          return new Response(rid, Response.ERROR, "Cannot add yourself", null);
        }

        User target = userDao.getById(String.valueOf(targetId));
        if (target == null) {
          return new Response(rid, Response.ERROR, "User not found", null);
        }

        String existing = friendDao.getRelationship(me.getId(), targetId);
        if (existing != null) {
          return new Response(rid, Response.ERROR, "Already " + existing.toLowerCase(), null);
        }

        boolean sent = friendDao.sendRequest(me.getId(), targetId);

        if (sent) {
          Friendship notif = new Friendship();
          notif.setRequesterId(me.getId());
          notif.setRequesterUsername(me.getUsername());
          notif.setRequesterAvatarUrl(me.getAvatarUrl());
          notif.setAddresseeId(targetId);
          notif.setAddresseeUsername(target.getUsername());
          notif.setAddresseeAvatarUrl(target.getAvatarUrl());
          notif.setStatus(Friendship.PENDING);

          AuctionManager.getInstance()
                  .sendToUser(targetId, new Response("", "FRIEND_REQUEST", "new_request", notif));
        }

        return new Response(rid, sent ? Response.OK : Response.ERROR, sent ? "Sent" : "Failed", null);
      }

      case Request.ACCEPT_FRIEND: {
        int requesterId = ((Number) request.getPayload()).intValue();
        boolean ok = friendDao.acceptRequest(requesterId, me.getId());

        if (ok) {
          User requester = userDao.getById(String.valueOf(requesterId));

          Friendship notif = new Friendship();
          notif.setRequesterId(requesterId);
          notif.setAddresseeId(me.getId());
          notif.setAddresseeUsername(me.getUsername());
          notif.setAddresseeAvatarUrl(me.getAvatarUrl());
          notif.setStatus(Friendship.ACCEPTED);

          if (requester != null) {
            notif.setRequesterUsername(requester.getUsername());
            notif.setRequesterAvatarUrl(requester.getAvatarUrl());
          }

          AuctionManager.getInstance()
                  .sendToUser(requesterId, new Response("", "FRIEND_ACCEPTED", "accepted", notif));
        }

        return new Response(rid, ok ? Response.OK : Response.ERROR, ok ? "Accepted" : "Failed", null);
      }

      case Request.DECLINE_FRIEND: {
        int requesterId = ((Number) request.getPayload()).intValue();
        boolean ok = friendDao.declineRequest(requesterId, me.getId());

        if (ok) {
          AuctionManager.getInstance()
                  .sendToUser(requesterId, new Response("", "FRIEND_DECLINED", "declined", me.getId()));
        }

        return new Response(rid, ok ? Response.OK : Response.ERROR, ok ? "Declined" : "Failed", null);
      }

      case Request.REMOVE_FRIEND: {
        int otherId = ((Number) request.getPayload()).intValue();
        boolean ok = friendDao.removeFriend(me.getId(), otherId);

        if (ok) {
          AuctionManager.getInstance()
                  .sendToUser(otherId, new Response("", "FRIEND_REMOVED", "removed", me.getId()));
        }

        return new Response(rid, ok ? Response.OK : Response.ERROR, ok ? "Removed" : "Failed", null);
      }

      case Request.GET_FRIENDS: {
        int userId = me.getId();

        List<Friendship> friends = friendDao.getFriends(userId);
        return new Response(rid, Response.OK, "success", (java.io.Serializable) friends);
      }

      case Request.GET_FRIEND_REQUESTS: {
        int userId = me.getId();

        List<Friendship> reqs = friendDao.getPendingRequests(userId);
        return new Response(rid, Response.OK, "success", (java.io.Serializable) reqs);
      }

      default:
        return new Response(rid, Response.ERROR, "Unknown friend action", null);
    }
  }
}