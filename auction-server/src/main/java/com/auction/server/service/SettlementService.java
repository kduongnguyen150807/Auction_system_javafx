package com.auction.server.service;

import com.auction.server.dao.ItemDao;
import com.auction.server.dao.UserDao;
import com.auction.server.dao.TransactionLogDao;
import com.auction.shared.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SettlementService {
    private ItemDao itemdao;
    private UserDao userdao;
    private TransactionLogDao logdao;

    public SettlementService() {
        this.itemdao = new ItemDao();
        this.userdao = new UserDao();
        this.logdao = new TransactionLogDao();
    }

    public void start() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                List<Item> res = itemdao.getexpireditems();
                for (Item res1 : res) {
                    settle(res1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void settle(Item res) {
        int res1 = getwinnerid(res.getid());
        if (res1 > 0) {
            double res2 = res.getcurrentprice();
            userdao.addbiddermetrics(res1, 0); // Thắng bid thì cộng 1 itemsbought

            User res3 = userdao.getbyid(String.valueOf(res.getsellerid()));
            if (res3 != null) {
                userdao.updatebalance(res3.getid(), res3.getbalance() + res2);
                userdao.addsellermetrics(res3.getid(), res2);
                logdao.insertlog(res3.getid(), "ITEM_SOLD", res2, res.getid());
                AuctionManager.getinstance().sendtouser(res3.getid(), new Response("", "BALANCE_UPDATE", "Success", userdao.getbyid(String.valueOf(res3.getid()))));
            }
            itemdao.closeauction(res.getid(), res1, "CLOSED");
        } else {
            itemdao.closeauction(res.getid(), 0, "EXPIRED");
        }
        AuctionManager.getinstance().broadcast(new Response("", "ITEM_CLOSED", "Success", res.getid()));
    }

    private int getwinnerid(int id) {
        int ans = -1;
        try {
            java.sql.Connection res = com.auction.server.dao.DatabaseConnection.getinstance().getconnection();
            String res1 = "SELECT userid FROM bid_transactions WHERE itemid = ? ORDER BY bidvalue DESC LIMIT 1";
            java.sql.PreparedStatement res2 = res.prepareStatement(res1);
            res2.setInt(1, id);
            java.sql.ResultSet res3 = res2.executeQuery();
            if (res3.next()) ans = res3.getInt("userid");
        } catch (Exception e) {}
        return ans;
    }
}