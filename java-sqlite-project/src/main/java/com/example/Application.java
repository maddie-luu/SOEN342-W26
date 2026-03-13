package com.example;

import com.example.persistence.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Main application entry point.
 * For US-1, this starts the command-line menu for the user.
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        logger.info("Starting Task Management application");

        try {
            DatabaseConnection.initializeDatabase();
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
        }

        TaskManagementCLI cli = new TaskManagementCLI();
        cli.run();
    }
}

