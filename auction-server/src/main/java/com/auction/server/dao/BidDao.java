package com.auction.server.dao;

import com.auction.server.Service.SQLService;
import com.auction.shared.BidTransaction;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.*;

public class BidDao {
    private Connection conn;
    private static BidDao instance;

    private BidDao(){
        this.conn = DatabaseConnection.getInstance().getConnection();
    }

    public static synchronized BidDao getInstance() {
        if(instance == null){
            instance = new BidDao();
        }
        return instance;
    }

    public boolean placeBid(BidTransaction b){
        String sql1 = "INSERT INTO bid_transactions(itemid,userid,bidvalue,timestamp) values(?,?,?,?)";
        List<Object> l1 = List.of(b.getItemId(), b.getUserId(), b.getBidValue(), Timestamp.valueOf(b.getTimestamp()));

        String sql2 = "UPDATE items SET currentprice = ? WHERE id = ?";
        List<Object> l2 = List.of(b.getBidValue(), b.getItemId());

        return SQLService.MultiUpdate(List.of(sql1, sql2), List.of(l1, l2), this.conn);
    }
}
