package com.auction.server.service;

import com.auction.server.dao.ItemDao;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ItemService {
    private final ItemDao itemDao;

    public ItemService() {
        this.itemDao = new ItemDao();
    }

    public boolean addLot(Map<String, String> data) {
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm[:ss]");

            LocalDateTime st =
                    LocalDateTime.parse(
                            data.get("starttime").length() == 16 ? data.get("starttime") + ":00" : data.get("starttime"),
                            f);

            LocalDateTime et =
                    LocalDateTime.parse(
                            data.get("endtime").length() == 16 ? data.get("endtime") + ":00" : data.get("endtime"),
                            f);

            return itemDao.insertLot(
                    data.get("name"),
                    data.get("description"),
                    Double.parseDouble(data.get("startingprice")),
                    Double.parseDouble(data.getOrDefault("maxprice", "0")),
                    st,
                    et,
                    data.get("sellerusername"),
                    data.getOrDefault("imageurl", ""),
                    data.getOrDefault("category", "Vehicle"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}