package com.auction.server.handler.live;

import com.auction.server.handler.dispatch.ActionHandler;

/** Groups live auction handlers for OCP-friendly registration. */
public final class LiveSessionHandlers {
  public ActionHandler join() {
    return new JoinLiveSessionHandler();
  }

  public ActionHandler leave() {
    return new LeaveLiveSessionHandler();
  }

  public ActionHandler tiers() {
    return new GetLiveBidTiersHandler();
  }

  public ActionHandler list() {
    return new GetLiveAuctionsHandler();
  }
}
