package com.example;

import java.io.IOException;

public interface TaskExportGateway {
    String exportTask(Task task, Project project, String outputFilePath) throws IOException;
    String exportProjectTasks(Project project, String outputFilePath) throws IOException;
}
