# Java SQLite Project

A Java project demonstrating a clean persistence layer with SQLite.

## Project Structure

```
src/
├── main/
│   ├── java/com/example/
│   │   ├── Application.java          # Main application entry point
│   │   ├── model/
│   │   │   └── User.java             # User entity model
│   │   └── persistence/
│   │       ├── DatabaseConnection.java # Database connection utility
│   │       └── UserDAO.java           # Data Access Object for Users
│   └── resources/
│       └── logback.xml               # Logging configuration
└── test/
    └── java/com/example/             # Unit tests

pom.xml                               # Maven configuration
```

## Features

- **SQLite Database**: Lightweight, file-based SQL database
- **DAO Pattern**: Clean separation of data access logic
- **Connection Pooling**: Utility class for database connections
- **Logging**: SLF4J with Logback for application logging
- **CRUD Operations**: Full Create, Read, Update, Delete functionality

## Dependencies

- **sqlite-jdbc**: SQLite JDBC driver (org.xerial:sqlite-jdbc)
- **SLF4J**: Simple Logging Facade for Java
- **Logback**: SLF4J implementation
- **JUnit**: Unit testing framework

## Building the Project

```bash
mvn clean install
```

## Running the Application

```bash
mvn exec:java -Dexec.mainClass="com.example.Application"
```

## Database

The SQLite database file (`app_database.db`) is created automatically in the project root when the application runs for the first time.

## Example Usage

The `Application` class demonstrates:
1. Database initialization
2. Creating users
3. Retrieving all users
4. Retrieving specific users (by ID and email)
5. Updating user information
6. Deleting users

## Extending the Project

To add more entities:
1. Create a model class in `com.example.model`
2. Create a corresponding DAO class in `com.example.persistence`
3. Add table creation method to `DatabaseConnection.initializeDatabase()`

## License

MIT
