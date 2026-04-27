package com.auction.server.service;

import com.auction.server.dao.BidDao;
import com.auction.server.dao.DatabaseConnection;
import com.auction.server.dao.ItemDao;
import com.auction.shared.BidTransaction;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import com.auction.shared.Response;

import java.sql.Connection;
import java.time.LocalDateTime;

public class BidService {
  private final ItemDao itemDao;

  public BidService() {
    this.itemDao = new ItemDao();
  }

  public Response placeBid(BidTransaction b) {
    Item item = this.itemDao.getById(b.getItemId());

    if (item == null || item.getStatus() != ItemStatus.OPEN) {
      return new Response("sys", Response.ERROR, "closed", null);
    }

    if (b.getBidValue() <= item.getCurrentPrice()) {
      return new Response("sys", Response.ERROR, "low", null);
    }

    try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
      conn.setAutoCommit(false);

      try {
        BidDao bidDao = new BidDao(conn);

        boolean inserted = bidDao.insertBid(b);
        if (!inserted) {
          conn.rollback();
          return new Response("sys", Response.ERROR, "fail", null);
        }

        boolean updated =
                this.itemDao.updatePrice(conn, item.getId(), b.getBidValue(), item.getVersion());
        if (!updated) {
          conn.rollback();
          return new Response("sys", Response.ERROR, "conflict", null);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = item.getEndTime();
        if (end != null && now.plusSeconds(30).isAfter(end)) {
          boolean endUpdated =
                  this.itemDao.updateEndTime(conn, item.getId(), end.plusSeconds(60));
          if (!endUpdated) {
            conn.rollback();
            return new Response("sys", Response.ERROR, "fail_endtime", null);
          }
        }

        conn.commit();
        return new Response("sys", Response.OK, "success", b);

      } catch (Exception e) {
        conn.rollback();
        e.printStackTrace();
        return new Response("sys", Response.ERROR, "fail", null);
      } finally {
        conn.setAutoCommit(true);
      }

    } catch (Exception e) {
      e.printStackTrace();
      return new Response("sys", Response.ERROR, "fail", null);
    }
  }
}