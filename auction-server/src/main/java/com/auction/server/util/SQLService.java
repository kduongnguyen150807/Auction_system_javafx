package com.auction.server.util;

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

    public static boolean MultiUpdate(List<String> sqls, List<List<Object>> objects, Connection conn){
        if (sqls == null || objects == null || sqls.size() != objects.size()) {
            return false;
        }
        try {
            conn.setAutoCommit(false);
            for (int i = 0; i< sqls.size(); i++){
                try(PreparedStatement ps = conn.prepareStatement(sqls.get(i))){
                    for(int j = 1; j<= objects.get(i).size(); j++){
                        ps.setObject(j, objects.get(i).get(j-1));
                    }
                    ps.executeUpdate();
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public static <T> List<T> Fetch(String sql, List<Object> args, Connection conn, ResultSetMapper<T> mapper) {
        List<T> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if(args!= null){
                for (int i = 0; i < args.size(); i++) {
                    ps.setObject(i + 1, args.get(i));
                }
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

    public static boolean Exists(String sql, List<Object> args, Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (args != null) {
                for (int i = 0; i < args.size(); i++) {
                    ps.setObject(i + 1, args.get(i));
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("SQL Exist Query Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

