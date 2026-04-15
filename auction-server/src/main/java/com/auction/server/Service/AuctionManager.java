package com.auction.server.Service;

import com.auction.server.controller.ClientHandler;
import com.auction.server.dao.ItemDao;
import com.auction.server.dao.TransactionLogDao;
import com.auction.server.dao.UserDao;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.Response;
import com.auction.shared.User;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {
    private static AuctionManager instance;
    private UserDao userDao;
    private TransactionLogDao logDao;
    private BidService bidService;
    private ItemDao itemDao;
    private List<ClientHandler> clients;

    private AuctionManager(){
        userDao = UserDao.getInstance();
        logDao = TransactionLogDao.getInstance();
        itemDao = ItemDao.getInstance();
        bidService = new BidService();
        this.clients = new CopyOnWriteArrayList<>();
    }

    public static AuctionManager getInstance(){
        if(instance == null){
            instance = new AuctionManager();
        }
        return instance;
    }

    public void sendToUser(int id, Response r) {
        for (ClientHandler client : this.clients) {
            if (client.getCurrentUser() != null && client.getCurrentUser().getId() == id) client.send(r);
        }
    }

    public void broadcast(Response r) {
        for (ClientHandler client : this.clients) client.send(r);
    }

    public void addClient(ClientHandler c) {
        this.clients.add(c);
    }

    public void removeClient(ClientHandler c) {
        this.clients.remove(c);
    }

    public synchronized Response processBid(BidTransaction bidTransaction){
        Item item = itemDao.getById(bidTransaction.getItemId());
        User user = userDao.getById(String.valueOf(bidTransaction.getUserId()));

        if(user!=null){
            String phoneNumber = user.getPhoneNumber();
            if(phoneNumber == null || phoneNumber.trim().isEmpty()){
                return new Response(
                        "", Response.ERROR, "Unverified account. Add a phone number to bid.", null);
            }
        }

        if(item!=null && item.getSellerId() == bidTransaction.getUserId()){
            return new Response("", Response.ERROR, "fail", null);
        }

        if(item!=null && bidTransaction.getBidValue() <= item.getCurrentPrice()){
            bidTransaction.setBidValue(item.getCurrentPrice() + bidTransaction.getAutoBidIncrement());
        }

        if(user!=null && user.getBalance() < bidTransaction.getBidValue()){
            return new Response("", Response.ERROR, "Fail", null);
        }

        //1. logic mua dut
        System.out.println(bidTransaction.getBidValue());
        System.out.println(item.getMaxPrice());
        if(item!=null && item.getMaxPrice() > 0 && bidTransaction.getBidValue() >= item.getMaxPrice()){
            System.out.println("mua dut thanh cong");
            //tru tien va update cho bidder
            double OldMax = item.getMaxPrice();
            userDao.updateBalance(user.getId(), user.getBalance() - OldMax);
            userDao.addBidderMetrics(user.getId(), OldMax);
            logDao.insertLog(user.getId(), "ITEM_BOUGHT", -OldMax, bidTransaction.getItemId());
            sendToUser(user.getId(), new Response(
                    "", "BALANCE_UPDATE", "Success", UserDao.getInstance().getById(String.valueOf(user.getId()))));
            //Cong tien va update cho seller
            User seller = userDao.getById(String.valueOf(item.getSellerId()));
            if (seller != null) {
                UserDao.getInstance().updateBalance(seller.getId(), seller.getBalance() + OldMax);
                userDao.addSellerMetrics(seller.getId(), OldMax);
                logDao.insertLog(seller.getId(), "ITEM_SOLD", OldMax, bidTransaction.getItemId());
                sendToUser(
                        seller.getId(),
                        new Response(
                                "", "BALANCE_UPDATE", "Success", userDao.getById(String.valueOf(seller.getId()))));
            }
            //update item va lot
            itemDao.updatePrice(item.getId(), OldMax, item.getVersion());
            itemDao.closeAuction(item.getId(), bidTransaction.getUserId(), "CLOSED");

            // announce client mon hang da bi mua dut
            Response buySuccessResponse = new Response("", Response.OK, "BUY_IT_NOW_SUCCESS", bidTransaction.getItemId());
            broadcast(buySuccessResponse);

            // chia buon voi nguoi tra gia truoc do
            int previousHighestBidderId = itemDao.getPreviousHighestBidder(bidTransaction.getItemId());
            if (previousHighestBidderId > 0) {
                Response outbidResponse = new Response("", "OUTBID_NOTIFY", "outbid", bidTransaction.getItemId());
                sendToUser(previousHighestBidderId, outbidResponse);
            }
            // 3. Lấy thông tin món hàng đã cập nhật để đồng bộ giao diện cho tất cả Client
            Item updatedItem = itemDao.getById(bidTransaction.getItemId());
            if (updatedItem != null) {
                Response updateResponse = new Response("", "NEW_BID_UPDATE", "priceupdate", updatedItem);
                broadcast(updateResponse);
            }
            return buySuccessResponse;
        }
        System.out.println("loi ky thuat");

        //tru tien nguoi dau gia moi va announce
        int oldUserId = itemDao.getPreviousHighestBidder(bidTransaction.getItemId());
        double OldPrice = item.getCurrentPrice();
        userDao.updateBalance(user.getId(), user.getBalance() - bidTransaction.getBidValue());
        logDao.insertLog(user.getId(), "BID_HOLD", -bidTransaction.getBidValue(), bidTransaction.getItemId());
        sendToUser(
                user.getId(),
                new Response(
                        "", "BALANCE_UPDATE", "Success", userDao.getById(String.valueOf(user.getId()))));
        //refund va announce cho bidder cu
        if(oldUserId > 0 && OldPrice > 0){
            User oldUser = userDao.getById(String.valueOf(oldUserId));
            if(oldUser != null){
                userDao.updateBalance(oldUserId, oldUser.getBalance() + OldPrice);
                logDao.insertLog(oldUserId, "BID_REFUND", OldPrice, bidTransaction.getItemId());
                sendToUser(
                        oldUserId,
                        new Response("", "BALANCE_UPDATE", "Outbid", oldUser));
            }
        }
        //update item
        Response NormalRes = this.bidService.placeBid(bidTransaction);
        if (NormalRes.getStatus().equals(Response.OK)) {
            
        }

    }
}
