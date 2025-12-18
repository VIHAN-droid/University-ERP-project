# University ERP System

A modern desktop application to manage university operations (courses, sections, grades, users) built in 100% Java (Swing UI).

## Features

**Students**
- Browse & register for courses/sections (with seat limits)
- Drop sections before deadlines
- View grades & download transcripts (CSV/PDF)
- Timetable view

**Instructors**
- Enter/update scores for their sections
- Set assessment weights (quiz, midterm, end-sem)
- View statistics (average, grade spread)
- Export gradesheets

**Admins**
- Create/manage all users, courses, sections
- Assign instructors
- Maintenance mode: system-wide read-only toggle
- System settings management

## Technology Stack

- Java 11+ & Swing (FlatLaf for modern UI)
- MySQL (auth_db & erp_db separation)
- HikariCP (connection pool)
- jBCrypt (secure password hashing)
- OpenCSV & OpenPDF (export features)
- Maven (build system)
- SLF4J + Logback (logging)

## Quick Start

**Requirements**
- JDK 11+
- MySQL 5.7+ or MariaDB 10.3+
- Maven 3.6+

**Database Setup**
```bash
mysql -u root -p
CREATE DATABASE auth_db;
CREATE DATABASE erp_db;
mysql -u root -p auth_db < database/auth_db_schema.sql
mysql -u root -p erp_db < database/erp_db_schema.sql
mysql -u root -p auth_db < database/auth_db_seed.sql
mysql -u root -p erp_db < database/erp_db_seed.sql
```
Edit `src/main/resources/application.properties` for DB credentials.

**Build & Run**
```bash
mvn clean package
java -jar target/AP-proj-ERP-1.0.0-jar-with-dependencies.jar
```
Or run with Maven/IDE targeting `edu.univ.erp.Main`.

## Default Users

| Username | Password    | Role       |
|----------|-------------|------------|
| admin1   | Supersid05  | ADMIN      |
| inst1    | Supersid05  | INSTRUCTOR |
| inst2    | Supersid05  | INSTRUCTOR |
| stu1     | Supersid05  | STUDENT    |
| stu2     | Supersid05  | STUDENT    |
| stu3     | Supersid05  | STUDENT    |

Change passwords after first login.

## Architecture

- **auth_db**: Authentication data only (user, roles, hashed passwords)
- **erp_db**: Academic data (students, instructors, sections, enrollments, grades)

**Main folders:**
```
src/main/java/edu/univ/erp/
  Main.java
  domain/      # model classes
  auth/        # authentication
  data/        # DAOs (database access)
  service/     # business logic
  access/      # access control
  ui/          # dashboards (student, instructor, admin)
  util/        # helpers
database/
  *.sql        # schema/seed scripts
```

## Security & Grade System

- Passwords with bcrypt, account lock after repeated failures
- Prepared statements to prevent SQL injection
- Session management & access control
- Grade breakdown: Quiz 20%, Midterm 30%, End-Sem 50% (configurable)
- Letter grades: A (90+), B (80+), C (70+), D (60+), F (<60)

## Maintenance Mode

Admins can toggle maintenance mode to make system read-only for users/instructors.

## Logs

App logs: `logs/erp-application.log`

## Troubleshooting

- DB connection errors? Check MySQL is running, credentials, firewall.
- Build errors? Run: `mvn clean install -U`
