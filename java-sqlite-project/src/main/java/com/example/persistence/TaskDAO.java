package com.example.persistence;

import com.example.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Task entity
 */
public class TaskDAO {
    private static final Logger logger = LoggerFactory.getLogger(TaskDAO.class);
    private static final String TABLE_NAME = "tasks";

    public static void createTableIfNotExists(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "created_date TEXT NOT NULL," +
                "priority_level TEXT," +
                "status TEXT NOT NULL," +
                "due_date TEXT," +
                "collaborator TEXT," +
                "collaborator_category TEXT," +
                "recurrence_type TEXT," +
                "recurrence_interval INTEGER," +
                "recurrence_start TEXT," +
                "recurrence_end TEXT" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            logger.info("Tasks table created or already exists");
        }
    }

    public static void insertTask(Task task) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + 
                " (title, description, created_date, priority_level, status, due_date, " +
                "collaborator, collaborator_category, recurrence_type, recurrence_interval, " +
                "recurrence_start, recurrence_end) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getCreatedDate() != null ? task.getCreatedDate().toString() : LocalDate.now().toString());
            pstmt.setString(4, task.getPriorityLevel());
            pstmt.setString(5, task.getStatus());
            pstmt.setString(6, task.getDuedate() != null ? task.getDuedate().toString() : null);
            pstmt.setString(7, task.getCollaborator());
            pstmt.setString(8, task.getCollaboratorCategory());
            pstmt.setString(9, task.getRecurrenceType());
            pstmt.setInt(10, task.getRecurrenceInterval());
            pstmt.setString(11, task.getRecurrenceStart() != null ? task.getRecurrenceStart().toString() : null);
            pstmt.setString(12, task.getRecurrenceEnd() != null ? task.getRecurrenceEnd().toString() : null);

            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.setId(generatedKeys.getInt(1));
                }
            }
            logger.info("Task inserted: {}", task.getTitle());
        }
    }

    public static List<Task> getAllTasks() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
        }
        return tasks;
    }

    public static Task getTaskById(int id) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTask(rs);
                }
            }
        }
        return null;
    }

    public static void updateTask(Task task) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + 
                " SET title=?, description=?, priority_level=?, status=?, due_date=?, " +
                "collaborator=?, collaborator_category=?, recurrence_type=?, recurrence_interval=?, " +
                "recurrence_start=?, recurrence_end=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getPriorityLevel());
            pstmt.setString(4, task.getStatus());
            pstmt.setString(5, task.getDuedate() != null ? task.getDuedate().toString() : null);
            pstmt.setString(6, task.getCollaborator());
            pstmt.setString(7, task.getCollaboratorCategory());
            pstmt.setString(8, task.getRecurrenceType());
            pstmt.setInt(9, task.getRecurrenceInterval());
            pstmt.setString(10, task.getRecurrenceStart() != null ? task.getRecurrenceStart().toString() : null);
            pstmt.setString(11, task.getRecurrenceEnd() != null ? task.getRecurrenceEnd().toString() : null);

            pstmt.executeUpdate();
            logger.info("Task updated: {}", task.getTitle());
        }
    }

    public static void deleteTask(int id) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logger.info("Task deleted with id: {}", id);
        }
    }

    private static Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setCreatedDate(rs.getString("created_date") != null ? LocalDate.parse(rs.getString("created_date")) : null);
        task.setPriorityLevel(rs.getString("priority_level"));
        task.setStatus(rs.getString("status"));
        task.setDuedate(rs.getString("due_date") != null ? LocalDate.parse(rs.getString("due_date")) : null);
        task.setCollaborator(rs.getString("collaborator"));
        task.setCollaboratorCategory(rs.getString("collaborator_category"));
        task.setRecurrenceType(rs.getString("recurrence_type"));
        task.setRecurrenceInterval(rs.getInt("recurrence_interval"));
        task.setRecurrenceStart(rs.getString("recurrence_start") != null ? LocalDate.parse(rs.getString("recurrence_start")) : null);
        task.setRecurrenceEnd(rs.getString("recurrence_end") != null ? LocalDate.parse(rs.getString("recurrence_end")) : null);
        return task;
    }
}
