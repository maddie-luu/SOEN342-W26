# SOEN 342 Personal Task Management System

## Project Overview
This repository contains a Concordia University SOEN 342 group project for a Personal Task Management System. The system is being developed as a Java command-line application with an object-oriented design and a SQLite-backed persistence layer. The project is requirements-driven and supported by UML and analysis artifacts included in the `docs/` directory.

## Course Context
This project was developed for **SOEN 342: Software Requirements and Deployment** at Concordia University. The repository reflects the progression of the course deliverables across Iteration I, Iteration II, and Iteration III, including system requirements, UML/domain modeling, system sequence diagrams, OCL constraints, and a proof-of-concept implementation.

---

## Team Members

| Name | Student ID | GitHub Username | GitHub Profile |
|------|------------|-----------------|----------------|
| Vincent de Serres | 40272920 | `vinnythepoo2` | [github.com/vinnythepoo2](https://github.com/vinnythepoo2) |
| Ahmed Eskaf | 40235587 | `A-Eskaf` | [github.com/A-Eskaf](https://github.com/A-Eskaf) |
| Madison Luu | 40282381 | `maddie-luu` | [github.com/maddie-luu](https://github.com/maddie-luu) |

---

## Features

### Iteration I - Core Features
- Create tasks with title, optional description, creation date, priority, status, and optional due date
- Manage tasks either independently or under at most one project
- Organize tasks with subtasks and tags
- Update task details after creation
- List, search, and view tasks in multiple ways
- Record activity history for task-related actions

### Iteration II - Extended Features
- Recurring tasks and task occurrences
- Project collaborators
- Collaborator categories: Senior, Intermediate, and Junior
- Open-task limits based on collaborator category
- Search by multiple criteria
- Export search results or database results to CSV
- Import tasks from CSV

### Iteration III - Advanced Features
- Export single task to iCal (.ics) format
- Export all project tasks to iCal (.ics) format
- Collaborator overload prevention (OCL constraint enforcement)
- OCL constraints for task and collaborator limits

---

## Technologies Used

| Technology | Purpose |
|------------|---------|
| Java 11 | Programming language |
| Maven | Build automation and dependency management |
| SQLite | Database persistence |
| SQLite JDBC | Database connectivity |
| SLF4J + Logback | Logging framework |
| JUnit | Unit testing |

---

## How to Configure and Run

### Prerequisites
- Java 11 or higher installed
- Maven installed
- Git installed

### Step 1: Clone the Repository
```bash
git clone https://github.com/maddie-luu/SOEN342-W26.git
cd SOEN342-W26
```

### Step 2: Build the Project
```bash
cd java-sqlite-project
mvn clean install
```

### Step 3: Run the Application
```bash
mvn exec:java -Dexec.mainClass="com.example.Application"
```

This launches the command-line Task Management System.

### Step 4: Using the Application
Once running, you will see a menu with options to:
1. Create tasks and projects
2. Assign tasks to projects
3. Edit tasks and projects
4. View and search tasks
5. Import/Export tasks (CSV and iCal)
6. View task history
7. And more...

---

## Project Structure

```
.
├── docs/                                    # UML, use cases, SSDs, OCL, and course artifacts
│   ├── OCL/                                 # OCL constraints
│   ├── SSD_Critical_Use_Cases/              # System Sequence Diagrams
│   └── Use_case_scenario/
├── java-sqlite-project/
│   ├── src/
│   │   ├── main/java/com/example/
│   │   │   ├── Application.java             # CLI entry point
│   │   │   ├── CollaboratorService.java     # Collaborator workload management
│   │   │   ├── TaskExportGateway.java       # Export interface (Gateway pattern)
│   │   │   ├── gateway/                     # Export implementations
│   │   │   ├── model/                       # Domain model classes
│   │   │   └── persistence/                 # DAO/Repository classes
│   │   └── resources/
│   └── pom.xml                              # Maven build configuration
├── app_database.db                          # SQLite database file
└── README.md
```

---

## Database Notes
- SQLite is used for persistence
- Database file: `app_database.db`
- Database is auto-initialized on first run

---

## OCL Constraints
The system enforces the following OCL constraints (see `docs/OCL/constraints.txt`):

1. **Task Subtask Limit**: A task cannot have more than 20 sub-tasks
2. **Open Tasks Without Due Date**: Cannot exceed 50 open tasks without a due date
3. **Collaborator Category Limits**: Senior (2), Intermediate (5), Junior (10) open tasks
4. **No Collaborator Overload**: Open task count must not exceed the category limit

---

## Demo Video
https://youtu.be/Q2HzAb_9qJ8
---
## License
This project is for educational purposes as part of SOEN 342 at Concordia University.
