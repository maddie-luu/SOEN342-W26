package com.example.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database connection utility class for SQLite
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static final String DATABASE_URL = "jdbc:sqlite:app_database.db";

    static {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load SQLite JDBC driver", e);
        }
    }

    /**
     * Get a connection to the SQLite database
     * @return Connection to the database
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    /**
     * Initialize database schema
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            UserDAO.createTableIfNotExists(conn);
            ProjectDAO.createTableIfNotExists(conn);
            TaskDAO.createTableIfNotExists(conn);
            SubtaskDAO.createTableIfNotExists(conn);
            TagDAO.createTableIfNotExists(conn);
            CollaboratorDAO.createTableIfNotExists(conn);
            ActivityDAO.createTableIfNotExists(conn);
            logger.info("Database initialized successfully");
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
        }
    }
}
