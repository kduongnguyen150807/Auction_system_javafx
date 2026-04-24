package com.auction.server.Service;

import com.auction.server.dao.ItemDao;
import com.auction.server.dao.TransactionLogDao;
import com.auction.server.dao.UserDao;
import com.auction.shared.Item.Item;
import com.auction.shared.Response;
import com.auction.shared.User.User;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SettlementService {
    private ItemDao itemDao = ItemDao.getInstance();
    private UserDao userDao = UserDao.getInstance();
    private TransactionLogDao transactionLogDao = TransactionLogDao.getInstance();

    public void start(){
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try{
                List<Item> ExpiredItems = itemDao.getExpiredItems();
                for(Item expiredItems: ExpiredItems){
                    settle(expiredItems);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void settle(Item item){
        int WinnerId = itemDao.getPreviousHighestBidder(item.getId());
        if( WinnerId > 0 ){
            double amount = item.getCurrentPrice();
            userDao.addBidderMetrics(WinnerId, amount);

            User Seller = userDao.getById(String.valueOf(item.getSellerId()));
            if(Seller!=null){
                userDao.updateBalance(Seller.getId(), Seller.getBalance() + amount);
                userDao.addSellerMetrics(Seller.getId(), amount);
                transactionLogDao.insertLog(Seller.getId(), "ITEM_SOLD", amount, item.getId());
                AuctionManager.getInstance().sendToUser(
                        Seller.getId(), new Response("", "BALANCE_UPDATE", "Success", Seller)
                );
            }
            itemDao.closeAuction(item.getId(), WinnerId, "CLOSED");
        }else{
            itemDao.closeAuction(item.getId(), 0, "EXPIRED");
        }
        AuctionManager.getInstance().broadcast(new Response("", "ITEM_CLOSED", "Success", item.getId()));
    }

}
