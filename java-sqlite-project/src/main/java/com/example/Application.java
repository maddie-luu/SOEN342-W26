package com.example;

import com.example.gateway.ICalTaskExporter;
import com.example.model.Task;
import com.example.model.TaskManagementCLI;
import com.example.persistence.DatabaseConnection;
import com.example.persistence.TaskDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Main application entry point.
 * Wires infrastructure dependencies (gateway implementations) into domain logic.
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

        TaskExportGateway exportGateway = new ICalTaskExporter();
        TaskManagementCLI cli = new TaskManagementCLI(exportGateway);

        try {
            List<Task> savedTasks = TaskDAO.getAllTasks();
            cli.tasks.addAll(savedTasks);
            logger.info("Loaded {} task(s) from database", savedTasks.size());
        } catch (Exception e) {
            logger.warn("Could not load tasks from database: {}", e.getMessage());
        }

        cli.run();
    }
}

