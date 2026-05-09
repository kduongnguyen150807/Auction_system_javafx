package com.auction.server.service.auction;

import com.auction.shared.BidTransaction;
import com.auction.shared.Response;
import java.util.List;
import java.util.Set;

/** Applies one manual bid inside an auction lock and schedules post-commit {@link Runnable}s. */
@FunctionalInterface
interface ManualBidExecutor {

  Response execute(BidTransaction bid, List<Runnable> after, Set<Integer> pendingPriceBroadcast);
}
