package com.auction.server.dao;

import com.auction.server.Service.ResultSetMapper;
import com.auction.server.Service.SQLService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public abstract class BaseDao {
    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    protected <T> List<T> executeFetch(String sql, List<Object> params, ResultSetMapper<T> mapper){
        try(Connection conn = getConnection()){
            return SQLService.Fetch(sql, params, conn, mapper);
        } catch (SQLException e) {
            System.err.println("Lỗi Fetch SQL: " + sql);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    protected boolean executeUpdate(String sql, List<Object> params) {
        try (Connection conn = getConnection()) {
            return com.auction.server.Service.SQLService.Update(sql, params, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi Update SQL: " + sql);
            e.printStackTrace();
            return false;
        }
    }

    protected boolean executeUpdate(List<String> sqls, List<List<Object>> paramsList) {
        try (Connection conn = getConnection()) {
            return com.auction.server.Service.SQLService.MultiUpdate(sqls, paramsList, conn);
        } catch (SQLException e) {
            System.err.println("Lỗi MultiUpdate SQL");
            e.printStackTrace();
            return false;
        }
    }
}
