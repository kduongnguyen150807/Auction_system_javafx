package com.auction.server.dao;

import com.auction.shared.BidFactory;
import com.auction.shared.BidTransaction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class BidDao extends BaseDao{
    private static BidDao instance;
    private BidDao(){}
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

        return executeUpdate(List.of(sql1, sql2), List.of(l1, l2));
    }

    public List<BidTransaction> GetBidHistory(int itemId){
        String sql = "SELECT * FROM bid_transactions WHERE itemid = ? ORDER BY timestamp ASC";
        return executeFetch(sql, List.of(itemId), this::mapBidTransaction);
    }

    private BidTransaction mapBidTransaction(ResultSet resultSet){
        try{
            BidTransaction bid = BidFactory.createBid(resultSet.getDouble("bidvalue"),
                    resultSet.getTimestamp("timestamp"));
            return bid;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("error mapping bid");
            return null;
        }
    }
}
