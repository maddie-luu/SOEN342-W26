package com.example.persistence;

import com.example.model.ActivityEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for ActivityEntry entity
 */
public class ActivityDAO {
    private static final Logger logger = LoggerFactory.getLogger(ActivityDAO.class);
    private static final String TABLE_NAME = "activity_history";

    public static void createTableIfNotExists(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "timestamp TEXT NOT NULL," +
                "description TEXT NOT NULL" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("Activity history table created or already exists");
        }
    }

    public static void insertActivity(ActivityEntry entry) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (timestamp, description) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.getTimestamp().toString());
            pstmt.setString(2, entry.getDescription());
            pstmt.executeUpdate();
            logger.info("Activity inserted: {}", entry.getDescription());
        }
    }

    public static List<ActivityEntry> getAllActivities() throws SQLException {
        List<ActivityEntry> activities = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY timestamp DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                activities.add(mapResultSetToActivity(rs));
            }
        }
        return activities;
    }

    public static void deleteOldActivities(int daysOld) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + 
                " WHERE timestamp < datetime('now', '-' || ? || ' days')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, daysOld);
            int deleted = pstmt.executeUpdate();
            logger.info("Deleted {} old activity records", deleted);
        }
    }

    private static ActivityEntry mapResultSetToActivity(ResultSet rs) throws SQLException {
        LocalDateTime timestamp = LocalDateTime.parse(rs.getString("timestamp"));
        return new ActivityEntry(timestamp, rs.getString("description"));
    }
}
