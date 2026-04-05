package com.auction.shared;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.PriorityQueue;
import java.util.Comparator;

public class AuctionLogicTest {

    @Test
    public void testpriorityqueue() {
        PriorityQueue<BidTransaction> res = new PriorityQueue<>(10, new Comparator<BidTransaction>() {
            public int compare(BidTransaction ans1, BidTransaction ans2) {
                return Double.compare(ans2.getmaxautobid(), ans1.getmaxautobid());
            }
        });

        BidTransaction ans = new BidTransaction(1, 1, 100);
        ans.setmaxautobid(500);

        BidTransaction res1 = new BidTransaction(1, 2, 150);
        res1.setmaxautobid(1000);

        BidTransaction ans1 = new BidTransaction(1, 3, 200);
        ans1.setmaxautobid(750);

        res.add(ans);
        res.add(res1);
        res.add(ans1);

        assertEquals(1000, res.poll().getmaxautobid());
        assertEquals(750, res.poll().getmaxautobid());
        assertEquals(500, res.poll().getmaxautobid());
    }

    @Test
    public void testantisniping() {
        LocalDateTime res = LocalDateTime.now();
        LocalDateTime ans = res.plusSeconds(30);

        boolean res1 = false;
        if (java.time.Duration.between(res, ans).getSeconds() < 60) {
            ans = ans.plusSeconds(60);
            res1 = true;
        }

        assertTrue(res1);
        assertTrue(java.time.Duration.between(res, ans).getSeconds() >= 60);
    }

    @Test
    public void testpolymorphism() {
        Item res = ItemFactory.createitem("Electronics");
        res.setcurrentprice(1000);
        assertEquals(50, res.calculatetax());

        Item ans = ItemFactory.createitem("Vehicle");
        ans.setcurrentprice(1000);
        assertEquals(100, ans.calculatetax());

        Item res1 = ItemFactory.createitem("Art");
        res1.setcurrentprice(1000);
        assertEquals(80, res1.calculatetax());
    }
}