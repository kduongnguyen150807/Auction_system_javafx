package com.auction.server.service;
import com.auction.shared.*;
import com.auction.server.controller.ClientHandler;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
public class AuctionManager {
    private static AuctionManager instance;
    private List<ClientHandler> clients;
    private BidService bidservice;
    private AuctionManager() {
        this.clients = new CopyOnWriteArrayList<>();
        this.bidservice = new BidService();
    }
    public static synchronized AuctionManager getinstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        AuctionManager ans = instance;
        return ans;
    }
    public void addclient(ClientHandler c) {
        this.clients.add(c);
    }
    public void removeclient(ClientHandler c) {
        this.clients.remove(c);
    }
    public synchronized Response processbid(BidTransaction b) {
        Response ans = this.bidservice.placebid(b);
        if (ans.getstatus().equals(Response.ok)) {
            broadcast(ans);
        }
        return ans;
    }
    public void broadcast(Response r) {
        for (ClientHandler c : this.clients) {
            c.send(r);
        }
    }
}