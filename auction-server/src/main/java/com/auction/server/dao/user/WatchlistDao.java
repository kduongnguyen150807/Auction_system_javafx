package com.auction.server.dao.user;

import com.auction.server.dao.platform.BaseDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WatchlistDao extends BaseDao<Integer> {

    @Override
    protected Integer mapRow(ResultSet rs) throws SQLException {
        return rs.getInt("item_id");
    }

    // Lấy toàn bộ ID sản phẩm mà user đang theo dõi
    public List<Integer> getUserWatchlist(int userId) {
        return queryList("SELECT item_id FROM watchlists WHERE user_id = ?", userId);
    }

    // Bật/Tắt theo dõi
    public boolean toggleWatchlist(int userId, int itemId, boolean isWatching) {
        if (isWatching) {
            return executeUpdate("INSERT IGNORE INTO watchlists (user_id, item_id) VALUES (?, ?)", userId, itemId);
        } else {
            return executeUpdate("DELETE FROM watchlists WHERE user_id = ? AND item_id = ?", userId, itemId);
        }
    }
}