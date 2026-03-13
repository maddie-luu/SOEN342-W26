# Personal Task Management System

## Project Overview
This module contains the Java implementation of the SOEN 342 Personal Task Management System. It is a command-line application built with Maven and backed by SQLite for persistence. The implementation follows an object-oriented, requirements-driven approach informed by the UML and analysis artifacts in the repository.

## Course Context
This project is part of **SOEN 342: Software Requirements and Deployment** at Concordia University. It represents the implementation side of the course project and aligns with the Iteration I core system requirements and the Iteration II proof-of-concept extensions.

## Core Features
- Single-user personal task management
- Task creation with title, optional description, creation date, priority, status, and optional due date
- Support for standalone tasks or tasks assigned to one project
- Support for subtasks and tags
- Task update workflows
- Task listing, search, and view operations
- Activity history for task-related actions

## Iteration II / PoC Features
- Recurring tasks and task occurrences
- Project collaborators
- Collaborator categories: Senior, Intermediate, and Junior
- Open-task limits by collaborator category
- Search by criteria
- CSV export for search results or database results
- CSV import for tasks
- Proof-of-concept focus on task search/view, CSV import, and CSV export
- Partial persistence layer for Iteration II features

## Project Structure
The current implementation still contains some scaffold naming from the starter codebase, but the intended architecture is organized around task management concerns.

```text
src/
├── main/
│   ├── java/com/example/
│   │   ├── Application.java            # Main command-line entry point
│   │   ├── TaskManagementCLI.java      # CLI menu and task-management interaction flow
│   │   ├── model/                      # Domain entities for tasks, projects, tags, collaborators, etc.
│   │   └── persistence/                # SQLite connection and persistence classes
│   └── resources/
│       └── logback.xml                 # Logging configuration
└── test/                               # Unit tests

pom.xml                                 # Maven configuration
```

## Technologies Used
- Java 11
- Maven
- SQLite
- SQLite JDBC
- SLF4J
- Logback
- JUnit

## Build Instructions
```bash
mvn clean install
```

## Run Instructions
```bash
mvn exec:java -Dexec.mainClass="com.example.Application"
```

## Database / Persistence Notes
- SQLite is used for the current persistence setup.
- The database file used by the project is `app_database.db`.
- Database initialization is wired into application startup.
- The persistence layer is currently partial and reflects the proof-of-concept state for Iteration II.

## Contributors
| Name | ID | GitHub |
|---|---:|---|
| Vincent de Serres | 40272920 | [vinnythepoo2](https://github.com/vinnythepoo2) |
| Ahmed Eskaf | 40235587 | [A-Eskaf](https://github.com/A-Eskaf) |
| Madison Luu | 40282381 | [maddie-luu](https://github.com/maddie-luu) |
