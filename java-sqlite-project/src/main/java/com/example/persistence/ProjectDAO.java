package com.example.persistence;

import com.example.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Project entity
 */
public class ProjectDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProjectDAO.class);
    private static final String TABLE_NAME = "projects";

    public static void createTableIfNotExists(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "description TEXT" +
                ")";

        String sqlProjectTasks = "CREATE TABLE IF NOT EXISTS project_tasks (" +
                "project_id INTEGER NOT NULL," +
                "task_id INTEGER NOT NULL," +
                "PRIMARY KEY(project_id, task_id)," +
                "FOREIGN KEY(project_id) REFERENCES projects(id)," +
                "FOREIGN KEY(task_id) REFERENCES tasks(id)" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            stmt.execute(sqlProjectTasks);
            logger.info("Projects table created or already exists");
        }
    }

    public static void insertProject(Project project) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (title, description) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, project.getTitle());
            pstmt.setString(2, project.getDescription());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    project.setId(generatedKeys.getInt(1));
                }
            }
            logger.info("Project inserted: {}", project.getTitle());
        }
    }

    public static void linkTaskToProject(int projectId, int taskId) throws SQLException {
        String sql = "INSERT OR IGNORE INTO project_tasks (project_id, task_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projectId);
            pstmt.setInt(2, taskId);
            pstmt.executeUpdate();
        }
    }

    public static List<Integer> getTaskIdsByProjectId(int projectId) throws SQLException {
        List<Integer> taskIds = new ArrayList<>();
        String sql = "SELECT task_id FROM project_tasks WHERE project_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, projectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    taskIds.add(rs.getInt("task_id"));
                }
            }
        }
        return taskIds;
    }

    public static List<Project> getAllProjects() throws SQLException {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                projects.add(mapResultSetToProject(rs));
            }
        }
        return projects;
    }

    public static Project getProjectById(int id) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProject(rs);
                }
            }
        }
        return null;
    }

    public static void updateProject(Project project) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET title=?, description=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, project.getTitle());
            pstmt.setString(2, project.getDescription());
            pstmt.executeUpdate();
            logger.info("Project updated: {}", project.getTitle());
        }
    }

    public static void deleteProject(int id) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logger.info("Project deleted with id: {}", id);
        }
    }

    private static Project mapResultSetToProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getInt("id"));
        project.setTitle(rs.getString("title"));
        project.setDescription(rs.getString("description"));
        return project;
    }
}
