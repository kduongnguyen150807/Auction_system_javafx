package com.auction.server.model;

import com.auction.shared.AuctionStatus;
import com.auction.shared.BidTransaction;
import com.auction.shared.Bidder;
import com.auction.shared.Item;
import com.auction.shared.exceptions.AuctionClosedException;
import com.auction.shared.exceptions.AuctionException;
import com.auction.shared.exceptions.InvalidBidException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Auction {
    private String auctionId;
    private Item item;
    private double currentHighestBid;
    private Bidder winningBidder;
    private AuctionStatus status;
    private LocalDateTime endTime;
    private List<BidTransaction> bidHistory = new ArrayList<>();

    private transient Timer timer;

    public Auction(String auctionId,Item item, double currentHighestBid, LocalDateTime endTime){
        this.auctionId = auctionId;
        this.item = item;
        this.currentHighestBid = currentHighestBid;
        this.endTime = endTime;
        this.status = AuctionStatus.OPEN;
    }
    public List<BidTransaction> getBidHistory() {
        return bidHistory;
    }
    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public Bidder getWinningBidder() {
        return winningBidder;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void startAuction(){
        this.status = AuctionStatus.RUNNING;
        scheduleAuctionEnd();
    }

    public synchronized boolean placeBid(Bidder bidder,double bidAmount) throws AuctionClosedException, InvalidBidException{
        if (this.status != AuctionStatus.RUNNING){
            throw new AuctionClosedException("Phiên đấu giá đã kết thúc hoặc chưa bắt đầu");
        }
        if (bidAmount <= this.currentHighestBid){
            throw new InvalidBidException("Giá đặt phải cao hơn giá hiện tại");
        }
        this.currentHighestBid = bidAmount;
        this.winningBidder = bidder;
        String transId = "TXN_" + System.currentTimeMillis();
        BidTransaction newTrans = new BidTransaction(this.item.getId(), bidder.getId(), bidAmount);
        this.bidHistory.add(newTrans);
        return true;
    }
    private void scheduleAuctionEnd() {
        long delayUntiEnd = 60000;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                closeAuction();
            }
        }, delayUntiEnd);
    }
    private synchronized void closeAuction() {
        if (this.status == AuctionStatus.RUNNING){
            this.status = AuctionStatus.FINISHED;
            System.out.println("Phiên đấu giá "+auctionId+" đã đóng");
            if (winningBidder != null){
                System.out.println("Người thắng cuộc: "+winningBidder.getFullName());
            }
            else {
                System.out.println("Không có người trả giá");
            }
        }
    }
}
