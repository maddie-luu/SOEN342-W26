package com.example;

import com.example.model.User;
import com.example.persistence.DatabaseConnection;
import com.example.persistence.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Main application class demonstrating SQLite persistence layer
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        try {
            // Initialize database
            DatabaseConnection.initializeDatabase();

            // Create and insert users
            logger.info("=== Creating Users ===");
            User user1 = new User("John Doe", "john@example.com", "555-1234");
            User user2 = new User("Jane Smith", "jane@example.com", "555-5678");
            User user3 = new User("Bob Johnson", "bob@example.com", "555-9012");

            UserDAO.insertUser(user1);
            UserDAO.insertUser(user2);
            UserDAO.insertUser(user3);

            // Retrieve all users
            logger.info("=== Retrieving All Users ===");
            List<User> allUsers = UserDAO.getAllUsers();
            allUsers.forEach(user -> logger.info("Retrieved: {}", user));

            // Retrieve a specific user
            logger.info("=== Retrieving User by ID ===");
            User retrievedUser = UserDAO.getUserById(1);
            if (retrievedUser != null) {
                logger.info("Found: {}", retrievedUser);
            }

            // Retrieve by email
            logger.info("=== Retrieving User by Email ===");
            User userByEmail = UserDAO.getUserByEmail("jane@example.com");
            if (userByEmail != null) {
                logger.info("Found: {}", userByEmail);
            }

            // Update a user
            logger.info("=== Updating User ===");
            User userToUpdate = UserDAO.getUserById(1);
            if (userToUpdate != null) {
                userToUpdate.setPhone("555-9999");
                UserDAO.updateUser(userToUpdate);
                logger.info("Updated: {}", userToUpdate);
            }

            // Delete a user
            logger.info("=== Deleting User ===");
            UserDAO.deleteUser(3);
            logger.info("Deleted user with id: 3");

            // Show final state
            logger.info("=== Final State ===");
            List<User> finalUsers = UserDAO.getAllUsers();
            finalUsers.forEach(user -> logger.info("Final: {}", user));

        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        }
    }
}
