package com.auction.server.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLService {
    public static boolean Update(String sql, List<Object> args, Connection connection){
        boolean ans = false;
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            for(int i = 1; i <= args.size(); i++ ){
                ps.setObject(i, args.get(i-1));
            }

            int rs = ps.executeUpdate();
            ans = rs > 0;
        }catch (SQLException e){
            System.out.println("Error updating");
            e.printStackTrace();
        }
        return ans;
    }

    public static <T> List<T> Fetch(String sql, List<Object> args, Connection conn, ResultSetMapper<T> mapper) {
        List<T> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Fetch Error: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }
}

