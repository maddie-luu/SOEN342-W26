package com.example.persistence;

import com.example.model.Subtask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Subtask entity
 */
public class SubtaskDAO {
    private static final Logger logger = LoggerFactory.getLogger(SubtaskDAO.class);
    private static final String TABLE_NAME = "subtasks";

    public static void createTableIfNotExists(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "task_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "status TEXT NOT NULL," +
                "FOREIGN KEY(task_id) REFERENCES tasks(id)" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("Subtasks table created or already exists");
        }
    }

    public static void insertSubtask(int taskId, Subtask subtask) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (task_id, title, description, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.setString(2, subtask.getTitle());
            pstmt.setString(3, subtask.getDescription());
            pstmt.setString(4, subtask.getStatus());
            pstmt.executeUpdate();
            logger.info("Subtask inserted for task id {}: {}", taskId, subtask.getTitle());
        }
    }

    public static List<Subtask> getSubtasksByTaskId(int taskId) throws SQLException {
        List<Subtask> subtasks = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    subtasks.add(mapResultSetToSubtask(rs));
                }
            }
        }
        return subtasks;
    }

    public static void updateSubtask(Subtask subtask) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET title=?, description=?, status=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subtask.getTitle());
            pstmt.setString(2, subtask.getDescription());
            pstmt.setString(3, subtask.getStatus());
            pstmt.executeUpdate();
            logger.info("Subtask updated: {}", subtask.getTitle());
        }
    }

    public static void deleteSubtask(int id) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logger.info("Subtask deleted with id: {}", id);
        }
    }

    public static void deleteSubtasksByTaskId(int taskId) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.executeUpdate();
            logger.info("Subtasks deleted for task id: {}", taskId);
        }
    }

    private static Subtask mapResultSetToSubtask(ResultSet rs) throws SQLException {
        Subtask subtask = new Subtask(rs.getString("title"), rs.getString("description"));
        subtask.setStatus(rs.getString("status"));
        return subtask;
    }
}
