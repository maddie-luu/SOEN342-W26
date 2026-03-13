# SOEN 342 Personal Task Management System

## Project Overview
This repository contains a Concordia University SOEN 342 group project for a Personal Task Management System. The system is being developed as a Java command-line application with an object-oriented design and a SQLite-backed persistence layer. The project is requirements-driven and supported by UML and analysis artifacts included in the `docs/` directory.

## Course Context
This project was developed for **SOEN 342: Software Requirements and Deployment** at Concordia University. The repository reflects the progression of the course deliverables across Iteration I and Iteration II, including system requirements, UML/domain modeling, system sequence diagrams, and a proof-of-concept implementation.

## Core Features
Iteration I defines the core single-user task management system:

- Create tasks with title, optional description, creation date, priority, status, and optional due date
- Manage tasks either independently or under at most one project
- Organize tasks with subtasks and tags
- Update task details after creation
- List, search, and view tasks in multiple ways
- Record activity history for task-related actions

## Iteration II / PoC Features
Iteration II extends the system and introduces proof-of-concept capabilities:

- Recurring tasks and task occurrences
- Project collaborators
- Collaborator categories: Senior, Intermediate, and Junior
- Open-task limits based on collaborator category
- Search by multiple criteria
- Export search results or database results to CSV
- Import tasks from CSV
- Proof of Concept scope:
  - Task Search and View
  - Import from CSV
  - Export to CSV
- Persistence for Iteration II is currently partial and implemented at proof-of-concept level

## Project Structure
The repository contains course documentation and a Java implementation module. The codebase still includes some starter/scaffold class names, but the intended architecture is task-management focused, as shown below.

```text
.
├── docs/                                # UML, use cases, SSDs, and supporting course artifacts
├── java-sqlite-project/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/
│   │       │   ├── Application.java    # Command-line application entry point
│   │       │   ├── TaskManagementCLI.java
│   │       │   ├── model/              # Domain model layer for tasks, projects, tags, collaborators, etc.
│   │       │   └── persistence/        # SQLite persistence layer and DAO/repository classes
│   │       └── resources/
│   │           └── logback.xml         # Logging configuration
│   ├── pom.xml                         # Maven build configuration
│   └── README.md                       # Implementation module notes
├── app_database.db                     # SQLite database file created/used by the current setup
└── README.md
```

## Technologies Used
- Java 11
- Maven
- SQLite
- SQLite JDBC (`org.xerial:sqlite-jdbc`)
- SLF4J and Logback
- JUnit

## Build Instructions
From the repository root:

```bash
cd java-sqlite-project
mvn clean install
```

## Run Instructions
From the repository root:

```bash
cd java-sqlite-project
mvn exec:java -Dexec.mainClass="com.example.Application"
```

This launches the current command-line application entry point for the task management system.

## Database / Persistence Notes
- SQLite is the persistence mechanism used in the current repository setup.
- The database file is `app_database.db`.
- The current implementation includes database initialization and a partial persistence layer aligned with the proof-of-concept stage.
- Iteration II persistence is not yet fully complete across all required features.

## Contributors
| Name | ID | GitHub |
|---|---:|---|
| Vincent de Serres | 40272920 | [vinnythepoo2](https://github.com/vinnythepoo2) |
| Ahmed Eskaf | 40235587 | [A-Eskaf](https://github.com/A-Eskaf) |
| Madison Luu | 40282381 | [maddie-luu](https://github.com/maddie-luu) |
