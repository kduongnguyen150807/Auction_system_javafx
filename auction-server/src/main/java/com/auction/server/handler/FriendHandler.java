package com.auction.server.handler;

import com.auction.server.dao.FriendDao;
import com.auction.server.dao.UserDao;
import com.auction.server.service.AuctionManager;
import com.auction.shared.*;
import java.util.List;

public class FriendHandler implements ActionHandler {
  private final FriendDao friendDao = new FriendDao();
  private final UserDao userDao = new UserDao();

  @Override
  public Response handle(Request request, HandlerContext context) {
    String action = request.getAction();
    String rid = request.getRequestId();
    User me = context.getCurrentUser();
    if (me == null) return new Response(rid, Response.ERROR, "Not logged in", null);

    switch (action) {
      case Request.ADD_FRIEND: {
        int targetId = ((Number) request.getPayload()).intValue();
        if (targetId == me.getId()) return new Response(rid, Response.ERROR, "Cannot add yourself", null);
        String existing = friendDao.getRelationship(me.getId(), targetId);
        if (existing != null) return new Response(rid, Response.ERROR, "Already " + existing.toLowerCase(), null);
        boolean sent = friendDao.sendRequest(me.getId(), targetId);
        if (sent) {
          User target = userDao.getById(String.valueOf(targetId));
          Friendship notif = new Friendship();
          notif.setRequesterId(me.getId());
          notif.setRequesterUsername(me.getUsername());
          notif.setRequesterAvatarUrl(me.getAvatarUrl());
          notif.setAddresseeId(targetId);
          if (target != null) {
            notif.setAddresseeUsername(target.getUsername());
            notif.setAddresseeAvatarUrl(target.getAvatarUrl());
          }
          notif.setStatus(Friendship.PENDING);
          AuctionManager.getInstance().sendToUser(targetId,
              new Response("", "FRIEND_REQUEST", "new_request", notif));
          AuctionManager.getInstance().sendToUser(me.getId(),
              new Response("", "FRIEND_REQUEST_SENT", "request_sent", notif));
        }
        return new Response(rid, sent ? Response.OK : Response.ERROR,
            sent ? "Request sent" : "Failed", null);
      }

      case Request.ACCEPT_FRIEND: {
        int requesterId = ((Number) request.getPayload()).intValue();
        boolean ok = friendDao.acceptRequest(requesterId, me.getId());
        if (ok) {
          User requester = userDao.getById(String.valueOf(requesterId));
          Friendship acceptedNotif = new Friendship();
          acceptedNotif.setRequesterId(requesterId);
          if (requester != null) {
            acceptedNotif.setRequesterUsername(requester.getUsername());
            acceptedNotif.setRequesterAvatarUrl(requester.getAvatarUrl());
          }
          acceptedNotif.setAddresseeId(me.getId());
          acceptedNotif.setAddresseeUsername(me.getUsername());
          acceptedNotif.setAddresseeAvatarUrl(me.getAvatarUrl());
          acceptedNotif.setStatus(Friendship.ACCEPTED);
          AuctionManager.getInstance().sendToUser(requesterId,
              new Response("", "FRIEND_ACCEPTED", "accepted", acceptedNotif));
          AuctionManager.getInstance().sendToUser(me.getId(),
              new Response("", "FRIEND_ACCEPTED", "accepted", acceptedNotif));
        }
        return new Response(rid, ok ? Response.OK : Response.ERROR,
            ok ? "Accepted" : "Failed", null);
      }

      case Request.DECLINE_FRIEND: {
        int requesterId = ((Number) request.getPayload()).intValue();
        boolean ok = friendDao.declineRequest(requesterId, me.getId());
        return new Response(rid, ok ? Response.OK : Response.ERROR,
            ok ? "Declined" : "Failed", null);
      }

      case Request.REMOVE_FRIEND: {
        int otherId = ((Number) request.getPayload()).intValue();
        boolean ok = friendDao.removeFriend(me.getId(), otherId);
        return new Response(rid, ok ? Response.OK : Response.ERROR,
            ok ? "Removed" : "Failed", null);
      }

      case Request.GET_FRIENDS: {
        int userId = ((Number) request.getPayload()).intValue();
        List<Friendship> friends = friendDao.getFriends(userId);
        return new Response(rid, Response.OK, "success", (java.io.Serializable) friends);
      }

      case Request.GET_FRIEND_REQUESTS: {
        int userId = ((Number) request.getPayload()).intValue();
        List<Friendship> reqs = friendDao.getPendingRequests(userId);
        return new Response(rid, Response.OK, "success", (java.io.Serializable) reqs);
      }

      default:
        return new Response(rid, Response.ERROR, "unknown", null);
    }
  }
}
