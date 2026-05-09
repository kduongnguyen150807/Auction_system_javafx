package com.auction.server.service;

import com.auction.server.dao.ItemDao;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemService {
    private static final Logger LOGGER = Logger.getLogger(ItemService.class.getName());
    private static final DateTimeFormatter LOT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm[:ss]");

    private final ItemDao itemDao;

    public ItemService() {
        this(new ItemDao());
    }

    public ItemService(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    public boolean addLot(Map<String, String> data) {
        try {
            LocalDateTime startTime = parseDateTime(data.get("starttime"));
            LocalDateTime endTime = parseDateTime(data.get("endtime"));

            return itemDao.insertLot(
                    data.get("name"),
                    data.get("description"),
                    parseDouble(data.get("startingprice")),
                    parseDouble(data.getOrDefault("maxprice", "0")),
                    startTime,
                    endTime,
                    data.get("sellerusername"),
                    data.getOrDefault("imageurl", ""),
                    data.getOrDefault("category", "Vehicle"));
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Invalid add lot payload", e);
            return false;
        }
    }

    private LocalDateTime parseDateTime(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            throw new IllegalArgumentException("date time is required");
        }

        String normalizedValue = rawValue.length() == 16 ? rawValue + ":00" : rawValue;
        return LocalDateTime.parse(normalizedValue, LOT_DATE_FORMAT);
    }

    private double parseDouble(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            throw new IllegalArgumentException("number is required");
        }

        return Double.parseDouble(rawValue);
    }
}