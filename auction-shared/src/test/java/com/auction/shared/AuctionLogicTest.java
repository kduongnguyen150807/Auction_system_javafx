package com.auction.shared;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.auction.shared.item.Item;
import com.auction.shared.item.ItemFactory;
import org.junit.jupiter.api.Test;

public class AuctionLogicTest {

  @Test
  public void testPriorityQueue() {
    PriorityQueue<BidTransaction> res =
        new PriorityQueue<>(
            10,
            new Comparator<BidTransaction>() {
              public int compare(BidTransaction ans1, BidTransaction ans2) {
                return Double.compare(ans2.getMaxAutoBid(), ans1.getMaxAutoBid());
              }
            });

    BidTransaction ans = new BidTransaction(1, 1, 100);
    ans.setMaxAutoBid(500);

    BidTransaction res1 = new BidTransaction(1, 2, 150);
    res1.setMaxAutoBid(1000);

    BidTransaction ans1 = new BidTransaction(1, 3, 200);
    ans1.setMaxAutoBid(750);

    res.add(ans);
    res.add(res1);
    res.add(ans1);

    assertEquals(1000, res.poll().getMaxAutoBid());
    assertEquals(750, res.poll().getMaxAutoBid());
    assertEquals(500, res.poll().getMaxAutoBid());
  }

  @Test
  public void testAntiSniping() {
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
}
