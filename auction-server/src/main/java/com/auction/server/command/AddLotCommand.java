package com.auction.server.command;

import com.auction.server.dao.ItemDao;
import com.auction.shared.Response;
import com.auction.shared.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class AddLotCommand implements Command {
    @Override
    public Response execute(Object data, String requestId, User user) {
        try {
            Map<String, String> lotData = (Map<String, String>) data;

            DateTimeFormatter dateTimeFormatter =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm[:ss]");

            String startTimeRaw = lotData.get("starttime");
            LocalDateTime startDateTime = LocalDateTime.parse(
                    startTimeRaw.length() == 16 ? startTimeRaw + ":00" : startTimeRaw,
                    dateTimeFormatter);

            String endTimeRaw = lotData.get("endtime");
            LocalDateTime endDateTime = LocalDateTime.parse(
                    endTimeRaw.length() == 16 ? endTimeRaw + ":00" : endTimeRaw,
                    dateTimeFormatter);

            boolean isSuccess = ItemDao.getInstance().insertLot(
                    lotData.get("name"),
                    lotData.get("description"),
                    Double.parseDouble(lotData.getOrDefault("startingprice", "0")),
                    Double.parseDouble(lotData.getOrDefault("maxprice", "0")),
                    startDateTime,
                    endDateTime,
                    lotData.get("sellerusername"),
                    lotData.getOrDefault("imageurl", ""),
                    lotData.getOrDefault("category", "Vehicle")
            );

            return new Response(
                    requestId,
                    isSuccess ? Response.OK : Response.ERROR,
                    isSuccess ? "Lot created successfully" : "Failed to create lot",
                    null
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(requestId, Response.ERROR, "System error: " + e.getMessage(), null);
        }
    }
}