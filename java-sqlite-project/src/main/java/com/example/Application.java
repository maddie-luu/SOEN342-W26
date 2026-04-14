package com.example;

import com.example.gateway.ICalTaskExporter;
import com.example.model.Task;
import com.example.model.TaskManagementCLI;
import com.example.persistence.DatabaseConnection;
import com.example.persistence.TaskDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import com.example.model.Project;
import com.example.model.Subtask;
import com.example.model.Tag;
import com.example.persistence.CollaboratorDAO;
import com.example.persistence.ProjectDAO;
import com.example.persistence.SubtaskDAO;
import com.example.persistence.TagDAO;

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

        // Build a lookup map by task id for fast wiring
        java.util.Map<Integer, Task> taskById = new java.util.HashMap<>();

        try {
            List<Task> savedTasks = TaskDAO.getAllTasks();
            cli.tasks.addAll(savedTasks);
            for (Task t : savedTasks) {
                taskById.put(t.getId(), t);
            }
            logger.info("Loaded {} task(s) from database", savedTasks.size());
        } catch (Exception e) {
            logger.warn("Could not load tasks from database: {}", e.getMessage());
        }

        // Load subtasks onto their parent tasks
        for (Task task : cli.tasks) {
            try {
                List<Subtask> subtasks = SubtaskDAO.getSubtasksByTaskId(task.getId());
                for (Subtask sub : subtasks) {
                    try { task.addSubtask(sub); } catch (Exception ignored) {}
                }
            } catch (Exception e) {
                logger.warn("Could not load subtasks for task {}: {}", task.getId(), e.getMessage());
            }
        }

        // Load tags onto their parent tasks
        for (Task task : cli.tasks) {
            try {
                List<Tag> tags = TagDAO.getTagsByTaskId(task.getId());
                for (Tag tag : tags) {
                    task.addTag(tag);
                }
            } catch (Exception e) {
                logger.warn("Could not load tags for task {}: {}", task.getId(), e.getMessage());
            }
        }

        // Load projects and wire their tasks using the junction table
        try {
            List<Project> savedProjects = ProjectDAO.getAllProjects();
            for (Project project : savedProjects) {
                try {
                    List<Integer> taskIds = ProjectDAO.getTaskIdsByProjectId(project.getId());
                    for (int taskId : taskIds) {
                        Task t = taskById.get(taskId);
                        if (t != null) {
                            project.addTask(t);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Could not load tasks for project {}: {}", project.getId(), e.getMessage());
                }
                cli.projects.add(project);
            }
            logger.info("Loaded {} project(s) from database", savedProjects.size());
        } catch (Exception e) {
            logger.warn("Could not load projects from database: {}", e.getMessage());
        }

        cli.run();
    }
}

