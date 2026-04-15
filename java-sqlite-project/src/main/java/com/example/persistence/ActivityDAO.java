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
                "task_id INTEGER," +
                "timestamp TEXT NOT NULL," +
                "description TEXT NOT NULL" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            // Migrate existing tables that predate the task_id column.
            try {
                stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN task_id INTEGER");
            } catch (SQLException ignored) {
                // Column already exists — no action needed.
            }
            logger.info("Activity history table created or already exists");
        }
    }

    public static void insertActivity(ActivityEntry entry) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (task_id, timestamp, description) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (entry.getTaskId() > 0) {
                pstmt.setInt(1, entry.getTaskId());
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }
            pstmt.setString(2, entry.getTimestamp().toString());
            pstmt.setString(3, entry.getDescription());
            pstmt.executeUpdate();
            logger.info("Activity inserted: {}", entry.getDescription());
        }
    }

    public static List<ActivityEntry> getAllActivities() throws SQLException {
        List<ActivityEntry> activities = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY timestamp ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                activities.add(mapResultSetToActivity(rs));
            }
        }
        return activities;
    }

    public static List<ActivityEntry> getActivitiesByTaskId(int taskId) throws SQLException {
        List<ActivityEntry> activities = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE task_id = ? ORDER BY timestamp ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(mapResultSetToActivity(rs));
                }
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
        int taskId = rs.getInt("task_id"); // returns 0 if NULL
        return new ActivityEntry(timestamp, taskId, rs.getString("description"));
    }
}
