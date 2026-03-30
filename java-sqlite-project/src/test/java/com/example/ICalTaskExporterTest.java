package com.example;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ICalTaskExporterTest {

    @Test
    public void exportTask_writesIcsWithRequiredFieldsAndSubtaskSummary() throws IOException {
        ICalTaskExporter exporter = new ICalTaskExporter();
        Task task = new Task();
        task.setTitle("Demo Task");
        task.setDescription("Prepare sprint notes");
        task.setDuedate(LocalDate.of(2026, 4, 18));
        task.setStatus("in progress");
        task.setPriorityLevel("high");
        task.addSubtask(new Subtask("Draft agenda", "Create first draft"));
        task.addSubtask(new Subtask("Share with team", "Send final agenda"));

        Project project = new Project("Course Project", "SOEN 342 Iteration III");
        Path tempDir = Files.createTempDirectory("ical-export-test");
        String outputPath = tempDir.resolve("single-task-export.ics").toString();

        String exportedFilePath = exporter.exportTask(task, project, outputPath);
        String content = Files.readString(Path.of(exportedFilePath), StandardCharsets.UTF_8);

        assertTrue(content.contains("BEGIN:VCALENDAR"));
        assertTrue(content.contains("BEGIN:VTODO"));
        assertTrue(content.contains("SUMMARY:Demo Task"));
        assertTrue(content.contains("DESCRIPTION:Prepare sprint notes"));
        assertTrue(content.contains("Subtasks:"));
        assertTrue(content.contains("- Draft agenda"));
        assertTrue(content.contains("- Share with team"));
        assertTrue(content.contains("DUE;VALUE=DATE:20260418"));
        assertTrue(content.contains("STATUS:IN-PROCESS"));
        assertTrue(content.contains("PRIORITY:1"));
        assertTrue(content.contains("CATEGORIES:Course Project"));
        assertTrue(content.contains("X-PROJECT-NAME:Course Project"));
    }

    @Test
    public void exportTask_throwsWhenTaskHasNoDueDate() throws IOException {
        ICalTaskExporter exporter = new ICalTaskExporter();
        Task task = new Task();
        task.setTitle("Task without date");
        task.setDescription("This should fail export");
        task.setPriorityLevel("medium");
        task.setStatus("open");

        Path tempDir = Files.createTempDirectory("ical-export-test-no-date");
        String outputPath = tempDir.resolve("missing-date.ics").toString();

        try {
            exporter.exportTask(task, null, outputPath);
            fail("Expected IllegalArgumentException for missing due date.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("due dates"));
        }
    }

    @Test
    public void exportTask_addsIcsExtensionWhenMissing() throws IOException {
        ICalTaskExporter exporter = new ICalTaskExporter();
        Task task = new Task();
        task.setTitle("Extension Check");
        task.setDescription("Verify extension logic");
        task.setDuedate(LocalDate.of(2026, 5, 1));
        task.setStatus("open");
        task.setPriorityLevel("low");

        Path tempDir = Files.createTempDirectory("ical-export-test-extension");
        String outputWithoutExtension = tempDir.resolve("extension-check").toString();

        String exportedFilePath = exporter.exportTask(task, null, outputWithoutExtension);
        assertTrue("Exporter should append .ics when missing", exportedFilePath.endsWith(".ics"));
        assertTrue("Exported file should exist", Files.exists(Path.of(exportedFilePath)));
    }
}
