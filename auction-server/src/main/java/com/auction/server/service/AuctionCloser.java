package com.auction.server.service;
import com.auction.shared.*;
import com.auction.server.dao.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class AuctionCloser {
    private ItemDao itemdao;
    private BidDao biddao;
    private ScheduledExecutorService scheduler;
    public AuctionCloser() {
        this.itemdao = new ItemDao();
        this.biddao = new BidDao();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    public void start() {
        Runnable task = () -> {
            try {
                List<Item> items = this.itemdao.getall();
                LocalDateTime now = LocalDateTime.now();
                for (Item i : items) {
                    if (i.getstatus() == ItemStatus.OPEN && i.getendtime().isBefore(now)) {
                        BidTransaction w = this.biddao.getwinner(i.getid());
                        if (w != null) {
                            this.itemdao.closeauction(i.getid(), w.getuserid(), "FINISHED");
                            Response ans = new Response("sys", Response.ok, "closed", i.getid());
                            AuctionManager.getinstance().broadcast(ans);
                        }
                    }
                }
            } catch (Exception e) {}
        };
        this.scheduler.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
    }
}