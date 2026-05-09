package com.auction.server.service.auction;

import com.auction.server.dao.auction.BidDao;
import com.auction.server.dao.auction.ItemDao;
import com.auction.server.dao.user.UserDao;
import com.auction.server.dao.wallet.TransactionLogDao;
import com.auction.shared.Item;
import com.auction.shared.Response;
import com.auction.shared.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.DelayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettlementService {
  private static final Logger logger = LoggerFactory.getLogger(SettlementService.class);
  private static final SettlementService instance = new SettlementService();
  private final ItemDao itemdao;
  private final UserDao userdao;
  private final BidDao biddao;
  private final TransactionLogDao logdao;
  private final DelayQueue<AuctionEndEvent> queue;

  private SettlementService() {
    this.itemdao = new ItemDao();
    this.userdao = new UserDao();
    this.biddao = new BidDao();
    this.logdao = new TransactionLogDao();
    this.queue = new DelayQueue<>();
  }

  public static SettlementService getInstance() {
    SettlementService ans = instance;
    return ans;
  }

  public void schedule(int itemid, LocalDateTime endtime) {
    AuctionEndEvent ans = new AuctionEndEvent(itemid, endtime);
    queue.remove(ans);
    queue.add(ans);
  }

  public void start() {
    List<Item> res = itemdao.getAll();
    for (Item item : res) {
      if ("OPEN".equals(item.getStatus().name()) && item.getEndTime() != null) {
        schedule(item.getId(), item.getEndTime());
      }
    }
    Thread thread = new Thread(() -> {
      while (true) {
        try {
          AuctionEndEvent ans = queue.take();
          Item item = itemdao.getById(ans.getItemid());
          if (item != null && "OPEN".equals(item.getStatus().name())) {
            settle(item);
          }
        } catch (Exception e) {
          logger.error("settlement_queue_error", e);
        }
      }
    });
    thread.setDaemon(true);
    thread.start();
  }

  private void settle(Item item) {
    int winnerid = biddao.getPreviousHighestBidder(item.getId());
    if (winnerid > 0) {
      boolean closed = itemdao.atomicCloseAuction(item.getId(), winnerid, "CLOSED");
      if (!closed) {
        return;
      }
      double finalprice = item.getCurrentPrice();
      userdao.addBidderMetrics(winnerid, finalprice);
      userdao.atomicCreditBalance(item.getSellerId(), finalprice);
      userdao.addSellerMetrics(item.getSellerId(), finalprice);
      logdao.insertLog(item.getSellerId(), "ITEM_SOLD", finalprice, item.getId());
      User freshseller = userdao.getById(String.valueOf(item.getSellerId()));
      if (freshseller != null) {
        Response res = new Response("", "BALANCE_UPDATE", "Success", freshseller);
        AuctionManager.getInstance().sendToUser(item.getSellerId(), res);
      }
      User ans = userdao.getById(String.valueOf(winnerid));
      if (ans != null) {
        AuctionManager.getInstance().getLeaderboardservice().updatescore(winnerid, ans.getUsername(), ans.getAvatarUrl(), finalprice);
      }
      AuctionManager.getInstance().broadcastleaderboard();
    } else {
      boolean closed = itemdao.atomicCloseAuction(item.getId(), 0, "EXPIRED");
      if (!closed) {
        return;
      }
    }
    Item closeditem = itemdao.getById(item.getId());
    if (closeditem != null) {
      Response res = new Response("", "ITEM_CLOSED", "closed", closeditem);
      AuctionManager.getInstance().broadcast(res);
    }
  }
}