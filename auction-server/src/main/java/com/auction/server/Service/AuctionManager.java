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

    public Response processBid(BidTransaction bidTransaction){
        Item item = itemDao.getById(bidTransaction.getItemId());
        User bidder = userDao.getById(String.valueOf(bidTransaction.getUserId()));

        Response validationError =  validateBid(item, bidder, bidTransaction);
        if(validationError != null) return validationError;

        if (isBuyItNow(item, bidTransaction)) {
            return handleBuyItNow(item, bidder, bidTransaction);
        } else {
            return handleNormalBid(item, bidder, bidTransaction);
        }
    }

    private Response handleNormalBid(Item item, User bidder, BidTransaction bid) {
        int oldBidderId = itemDao.getPreviousHighestBidder(item.getId());
        double oldPrice = item.getCurrentPrice();

        userDao.updateBalance(bidder.getId(), bidder.getBalance() - bid.getBidValue());
        logDao.insertLog(bidder.getId(), "BID_HOLD", -bid.getBidValue(), item.getId());
        sendToUser(bidder.getId(), new Response(
                        "", "BALANCE_UPDATE", "Success", bidder));

        if (oldBidderId > 0 && oldBidderId != bidder.getId()) {
            User oldUser = userDao.getById(String.valueOf(oldBidderId));
            if (oldUser != null) {
                userDao.updateBalance(oldBidderId, oldUser.getBalance() + oldPrice);
                logDao.insertLog(oldBidderId, "BID_REFUND", oldPrice, item.getId());
                sendToUser(oldUser.getId(), new Response("", "BALANCE_UPDATE", "Outbid", oldUser));
            }
        }

        Response result = this.bidService.placeBid(bid);

        if (result.getStatus().equals(Response.OK)) {
            java.time.LocalDateTime res5 = java.time.LocalDateTime.now();
            java.time.LocalDateTime ans5 = item.getEndTime();
            if (ans5 != null && java.time.Duration.between(res5, ans5).getSeconds() < 60) {
                java.time.LocalDateTime res6 = ans5.plusSeconds(60);
                itemDao.updateEndtime(item.getId(), res6);
                item.setEndTime(res6);
            }
        }

        broadcast(result);
        return result;
    }

    private Response handleBuyItNow(Item item, User bidder, BidTransaction bid) {
        double price = item.getMaxPrice();

        // Cập nhật Bidder
        userDao.updateBalance(bidder.getId(), bidder.getBalance() - price);
        userDao.addBidderMetrics(bidder.getId(), price);
        logDao.insertLog(bidder.getId(), "ITEM_BOUGHT", -price, item.getId());
        sendToUser(bidder.getId(), new Response(
                "", "BALANCE_UPDATE", "Success", UserDao.getInstance().getById(String.valueOf(bidder.getId()))));

        // Cập nhật Seller
        User seller = userDao.getById(String.valueOf(item.getSellerId()));
        if (seller != null) {
            userDao.updateBalance(seller.getId(), seller.getBalance() + price);
            userDao.addSellerMetrics(seller.getId(), price);
            logDao.insertLog(seller.getId(), "ITEM_SOLD", price, item.getId());
            sendToUser(seller.getId(), new Response(
                    "", "BALANCE_UPDATE", "Success", userDao.getById(String.valueOf(seller.getId()))));

        }

        // Chốt Item
        itemDao.updatePrice(item.getId(), price, item.getVersion());
        itemDao.closeAuction(item.getId(), bidder.getId(), "CLOSED");

        // Thông báo toàn hệ thống
        Response successRes = new Response("sys", Response.OK, "BUY_IT_NOW_SUCCESS", item.getId());
        broadcast(successRes);
        int previousHighestBidderId = itemDao.getPreviousHighestBidder(bid.getItemId());

        if (previousHighestBidderId > 0) {
            Response outbidResponse = new Response("", "OUTBID_NOTIFY", "outbid", bid.getItemId());
            sendToUser(previousHighestBidderId, outbidResponse);
        }
        return successRes;
    }

    private Response validateBid(Item item, User bidder, BidTransaction bid){
        if ("CLOSED".equals(String.valueOf(item.getStatus()))) {
            return new Response("", Response.ERROR, "please refresh your fucking page", null);
        }

        if (bidder != null && (bidder.getPhoneNumber() == null || bidder.getPhoneNumber().trim().isEmpty())) {
            return new Response("", Response.ERROR, "Unverified account. Add a phone number to bid.", null);
        }
        if (item.getSellerId() == bid.getUserId()) {
            return new Response("", Response.ERROR, "fail", null);
        }

        if (bid.getBidValue() <= item.getCurrentPrice()) {
            return new Response("", Response.ERROR, "Bid price is too low. Please refresh and try again.", null);
        }

        if (bidder != null && bidder.getBalance() < bid.getBidValue()) {
            return new Response("", Response.ERROR, "Fail", null);
        }

        return null;
    }

    private boolean isBuyItNow(Item item, BidTransaction bid) {
        return item.getMaxPrice() > 0 && bid.getBidValue() >= item.getMaxPrice();
    }
}
