package com.example.persistence;

import com.example.model.Collaborator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Collaborator entity
 */
public class CollaboratorDAO {
    private static final Logger logger = LoggerFactory.getLogger(CollaboratorDAO.class);
    private static final String TABLE_NAME = "collaborators";

    public static void createTableIfNotExists(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT UNIQUE NOT NULL," +
                "category TEXT NOT NULL" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("Collaborators table created or already exists");
        }
    }

    public static void insertCollaborator(Collaborator collaborator) throws SQLException {
        String sql = "INSERT OR IGNORE INTO " + TABLE_NAME + " (name, category) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, collaborator.getName());
            pstmt.setString(2, collaborator.getCategory());
            pstmt.executeUpdate();
            logger.info("Collaborator inserted: {}", collaborator.getName());
        }
    }

    public static List<Collaborator> getAllCollaborators() throws SQLException {
        List<Collaborator> collaborators = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                collaborators.add(mapResultSetToCollaborator(rs));
            }
        }
        return collaborators;
    }

    public static Collaborator getCollaboratorByName(String name) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCollaborator(rs);
                }
            }
        }
        return null;
    }

    public static void updateCollaborator(Collaborator collaborator) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET category=? WHERE name=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, collaborator.getCategory());
            pstmt.setString(2, collaborator.getName());
            pstmt.executeUpdate();
            logger.info("Collaborator updated: {}", collaborator.getName());
        }
    }

    public static void deleteCollaborator(String name) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            logger.info("Collaborator deleted: {}", name);
        }
    }

    private static Collaborator mapResultSetToCollaborator(ResultSet rs) throws SQLException {
        return new Collaborator(rs.getString("name"), rs.getString("category"));
    }
}
