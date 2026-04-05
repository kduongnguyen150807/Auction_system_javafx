package com.auction.server.service;

import com.auction.server.dao.ItemDao;
import com.auction.server.dao.TransactionLogDao;
import com.auction.server.dao.UserDao;
import com.auction.shared.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SettlementService {
  private ItemDao itemDao;
  private UserDao userDao;
  private TransactionLogDao logDao;

  public SettlementService() {
    this.itemDao = new ItemDao();
    this.userDao = new UserDao();
    this.logDao = new TransactionLogDao();
  }

  public void start() {
    Executors.newSingleThreadScheduledExecutor()
        .scheduleAtFixedRate(
            () -> {
              try {
                List<Item> res = itemDao.getExpiredItems();
                for (Item res1 : res) {
                  settle(res1);
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            },
            0,
            10,
            TimeUnit.SECONDS);
  }

  private void settle(Item res) {
    int res1 = getWinnerId(res.getId());
    if (res1 > 0) {
      double res2 = res.getCurrentPrice();
      userDao.addBidderMetrics(res1, 0); // Thắng bid thì cộng 1 itemsbought

      User res3 = userDao.getById(String.valueOf(res.getSellerId()));
      if (res3 != null) {
        userDao.updateBalance(res3.getId(), res3.getBalance() + res2);
        userDao.addSellerMetrics(res3.getId(), res2);
        logDao.insertLog(res3.getId(), "ITEM_SOLD", res2, res.getId());
        AuctionManager.getInstance()
            .sendToUser(
                res3.getId(),
                new Response(
                    "",
                    "BALANCE_UPDATE",
                    "Success",
                    userDao.getById(String.valueOf(res3.getId()))));
      }
      itemDao.closeAuction(res.getId(), res1, "CLOSED");
    } else {
      itemDao.closeAuction(res.getId(), 0, "EXPIRED");
    }
    AuctionManager.getInstance().broadcast(new Response("", "ITEM_CLOSED", "Success", res.getId()));
  }

  private int getWinnerId(int id) {
    int ans = -1;
    try {
      java.sql.Connection res =
          com.auction.server.dao.DatabaseConnection.getInstance().getConnection();
      String res1 =
          "SELECT userid FROM bid_transactions WHERE itemid = ? ORDER BY bidvalue DESC LIMIT 1";
      java.sql.PreparedStatement res2 = res.prepareStatement(res1);
      res2.setInt(1, id);
      java.sql.ResultSet res3 = res2.executeQuery();
      if (res3.next()) ans = res3.getInt("userid");
    } catch (Exception e) {
    }
    return ans;
  }
}
