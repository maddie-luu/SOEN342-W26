package com.example.persistence;

import com.example.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Tag entity
 */
public class TagDAO {
    private static final Logger logger = LoggerFactory.getLogger(TagDAO.class);
    private static final String TABLE_NAME = "tags";
    private static final String JUNCTION_TABLE = "task_tags";

    public static void createTableIfNotExists(Connection conn) throws SQLException {
        String sqlTags = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT UNIQUE NOT NULL" +
                ")";

        String sqlTaskTags = "CREATE TABLE IF NOT EXISTS " + JUNCTION_TABLE + " (" +
                "task_id INTEGER NOT NULL," +
                "tag_id INTEGER NOT NULL," +
                "PRIMARY KEY(task_id, tag_id)," +
                "FOREIGN KEY(task_id) REFERENCES tasks(id)," +
                "FOREIGN KEY(tag_id) REFERENCES tags(id)" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlTags);
            stmt.execute(sqlTaskTags);
            logger.info("Tags tables created or already exist");
        }
    }

    public static void insertTag(Tag tag) throws SQLException {
        String sql = "INSERT OR IGNORE INTO " + TABLE_NAME + " (name) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tag.getName());
            pstmt.executeUpdate();
            logger.info("Tag inserted: {}", tag.getName());
        }
    }

    public static int getOrCreateTagId(String tagName) throws SQLException {
        String selectSql = "SELECT id FROM " + TABLE_NAME + " WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, tagName);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        String insertSql = "INSERT OR IGNORE INTO " + TABLE_NAME + " (name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, tagName);
            insertStmt.executeUpdate();
            try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        // Re-fetch in case INSERT OR IGNORE silently skipped due to race
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, tagName);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        throw new SQLException("Could not get or create tag: " + tagName);
    }

    public static void addTagToTask(int taskId, int tagId) throws SQLException {
        String sql = "INSERT OR IGNORE INTO " + JUNCTION_TABLE + " (task_id, tag_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);
            pstmt.setInt(2, tagId);
            pstmt.executeUpdate();
        }
    }

    public static List<Tag> getTagsByTaskId(int taskId) throws SQLException {
        List<Tag> tags = new ArrayList<>();
        String sql = "SELECT t.id, t.name FROM " + TABLE_NAME + " t " +
                "INNER JOIN " + JUNCTION_TABLE + " tt ON t.id = tt.tag_id " +
                "WHERE tt.task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, taskId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tags.add(new Tag(rs.getString("name")));
                }
            }
        }
        return tags;
    }

    public static List<Tag> getAllTags() throws SQLException {
        List<Tag> tags = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tags.add(new Tag(rs.getString("name")));
            }
        }
        return tags;
    }

    public static void deleteTag(int id) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logger.info("Tag deleted with id: {}", id);
        }
    }
}
