# EduHive Annonce Management System

A JavaFX application for managing announcements in the EduHive system.

## Prerequisites

- Java JDK 17 or later
- Maven 3.6 or later
- MySQL Server

## Database Setup

1. Create a MySQL database named `eduhive_database`
2. Execute the following SQL to create the required table:

```sql
CREATE TABLE annonce (
    id INT(11) PRIMARY KEY AUTO_INCREMENT,
    titre VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    categorie VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## Configuration

The database connection settings are in `AnnonceService.java`. By default:
- URL: jdbc:mysql://localhost:3306/eduhive_database
- Username: root
- Password: (empty)

Modify these values if your MySQL setup is different.

## Building and Running

1. Clone the repository
2. Navigate to the project directory
3. Build the project:
```bash
mvn clean install
```
4. Run the application:
```bash
mvn javafx:run
```

## Features

- Add new announcements
- View all announcements in a table
- Update existing announcements
- Delete announcements
- Form validation and error handling
- Real-time database operations

## Project Structure

- `src/main/java/com/eduhive/`
  - `Main.java` - Application entry point
  - `entity/Annonce.java` - Announcement entity class
  - `service/AnnonceService.java` - Database operations
  - `controller/AnnonceController.java` - UI controller
- `src/main/resources/com/eduhive/`
  - `annonce-view.fxml` - JavaFX UI layout 