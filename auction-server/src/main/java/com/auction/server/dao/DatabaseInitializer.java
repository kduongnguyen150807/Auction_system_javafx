package com.auction.server.dao;

import java.sql.*;

public class DatabaseInitializer {

    public static void init() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            ensureItemColumns(conn);
            ensureUserColumns(conn);
            ensureIndexes(conn);

            System.out.println("Database initialized!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void ensureItemColumns(Connection conn) throws SQLException {
        if (!columnExists(conn, "items", "endtime")) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE items ADD COLUMN endtime TIMESTAMP");
            }
        }
    }
    private static void ensureUserColumns(Connection conn) throws SQLException {
        if (!columnExists(conn, "users", "moneyspent")) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE users ADD COLUMN moneyspent DOUBLE DEFAULT 0");
            }
        }

        if (!columnExists(conn, "users", "moneyreceived")) {
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("ALTER TABLE users ADD COLUMN moneyreceived DOUBLE DEFAULT 0");
            }
        }
    }
    private static void ensureIndexes(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_item_id ON bid_transactions(itemid)");
        }
    }
    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.columns WHERE table_name=? AND column_name=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, table);
            ps.setString(2, column);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}