package com.auction.server.dao.user;

import com.auction.server.dao.platform.BaseDao;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class WatchlistDao extends BaseDao<Integer> {

    private static final String SELECT_USER_WATCHLIST_SQL =
            "SELECT item_id FROM watchlists WHERE user_id = ?";

    private static final String INSERT_WATCHLIST_SQL =
            "INSERT IGNORE INTO watchlists (user_id, item_id) VALUES (?, ?)";

    private static final String DELETE_WATCHLIST_SQL =
            "DELETE FROM watchlists WHERE user_id = ? AND item_id = ?";

    @Override
    protected Integer mapRow(ResultSet rs) throws SQLException {
        return rs.getInt("item_id");
    }

    public List<Integer> getUserWatchlist(int userId) {
        return queryList(SELECT_USER_WATCHLIST_SQL, userId);
    }

    public boolean toggleWatchlist(int userId, int itemId, boolean isWatching) {
        if (isWatching) {
            return addToWatchlist(userId, itemId);
        }

        return removeFromWatchlist(userId, itemId);
    }

    private boolean addToWatchlist(int userId, int itemId) {
        return executeUpdate(INSERT_WATCHLIST_SQL, userId, itemId);
    }

    private boolean removeFromWatchlist(int userId, int itemId) {
        return executeUpdate(DELETE_WATCHLIST_SQL, userId, itemId);
    }
}