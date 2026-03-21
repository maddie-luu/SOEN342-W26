package com.example.persistence;

import com.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for User entity
 * Handles all database operations for Users
 */
public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private static final String TABLE_NAME = "users";

    /**
     * Create the users table if it doesn't exist
     */
    public static void createTableIfNotExists(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "phone TEXT" +
                ")";
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("Users table created or already exists");
        }
    }

    /**
     * Insert a new user into the database
     */
    public static void insertUser(User user) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (name, email, phone) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
            pstmt.executeUpdate();
            logger.info("User inserted: {}", user.getName());
        }
    }

    /**
     * Retrieve a user by ID
     */
    public static User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieve a user by email
     */
    public static User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieve all users
     */
    public static List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    /**
     * Update an existing user
     */
    public static void updateUser(User user) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET name = ?, email = ?, phone = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
            pstmt.setInt(4, user.getId());
            pstmt.executeUpdate();
            logger.info("User updated: {}", user.getName());
        }
    }

    /**
     * Delete a user by ID
     */
    public static void deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logger.info("User deleted with id: {}", id);
        }
    }

    /**
     * Map ResultSet to User object
     */
    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone")
        );
    }
}
