package com.example;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.example.model.Project;
import com.example.model.Task;

public interface TaskExportGateway {
    String exportTask(Task task, Project project, String outputFilePath) throws IOException;
    String exportProjectTasks(Project project, String outputFilePath) throws IOException;
    String exportFilteredTasks(List<Task> filteredTasks, Map<Task, Project> taskToProject, String outputFilePath) throws IOException;
}
