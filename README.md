# University ERP System

A comprehensive desktop Enterprise Resource Planning (ERP) system built for university course and grade management with role-based access control.

## 🎯 Overview

This Java-based ERP system provides a complete solution for managing university operations including student enrollments, instructor grading, course catalog management, and administrative controls. The application features a modern dark-themed UI built with Swing and FlatLaf, MySQL databases for persistent storage, and implements security best practices including password hashing and account lockout mechanisms.

## ✨ Key Features

### 🔐 Authentication & Security
- Secure login system with BCrypt password hashing
- Account lockout after 5 failed login attempts
- Role-based access control (Student, Instructor, Admin)
- Password change functionality with validation
- Session management

### 👨‍🎓 Student Features
- **Course Catalog**: Browse available courses with real-time seat availability
- **Registration**: Enroll in courses with duplicate and capacity checks
- **Timetable**: Visual weekly schedule of registered courses
- **Grades**: View detailed grade breakdowns by component (Quiz, Midterm, Final, etc.)
- **Transcript Export**: Generate PDF/CSV transcripts with CGPA calculation
- **CGPA Tracking**: Automatic calculation using 10-point grading scale

### 👨‍🏫 Instructor Features
- **Section Management**: View assigned courses and enrolled students
- **Grade Entry**: Enter grades with component-based weightage (must total 100%)
- **Grade Components**: Support for Quizzes, Midterms, Finals, Assignments, Labs, Projects
- **Bulk Operations**: Export class grades to CSV/PDF
- **Class Statistics**: View enrollment and grade distribution analytics
- **Grade Editing**: Modify existing grades with validation

### 👨‍💼 Admin Features
- **User Management**: Create users (Students, Instructors, Admins) with profile data
- **Account Control**: Lock/unlock accounts, reset passwords
- **Course Management**: Create and edit courses with full CRUD operations
- **Section Management**: Create sections with instructor assignment and capacity management
- **System Controls**:
  - Maintenance mode (read-only system-wide)
  - Add/Drop period toggle (control student registration windows)
- **Database Backup/Restore**: Full SQL backup and restore functionality

## 🏗️ Architecture

### Technology Stack
- **Frontend**: Java Swing with FlatLaf Look & Feel
- **Backend**: Java 11
- **Database**: MySQL (Dual database design)
  - `auth_db`: User authentication and credentials
  - `erp_db`: ERP data (students, courses, sections, grades)
- **Connection Pooling**: HikariCP for efficient database connections
- **Build Tool**: Maven
- **Logging**: SLF4J with Logback

### Design Patterns
- **Singleton**: DatabaseManager, SessionManager, AccessControl
- **DAO Pattern**: Separate data access layer for each entity
- **Service Layer**: Business logic abstraction (StudentService, InstructorService, AdminService)
- **MVC Architecture**: Clear separation of concerns

### Database Schema

**auth_db**:
```sql
users_auth (user_id, username, role, password_hash, status, failed_login_attempts)
password_history (id, user_id, password_hash, changed_at)
```

**erp_db**:
```sql
students (student_id, user_id, roll_no, program, year)
instructors (instructor_id, user_id, employee_id, department)
courses (course_id, code, title, credits, description)
sections (section_id, course_id, instructor_id, section_code, semester, year, capacity)
enrollments (enrollment_id, student_id, section_id, status, drop_deadline)
grades (grade_id, enrollment_id, component, score, max_score, weightage)
settings (setting_key, setting_value)
```

## 🚀 Getting Started

### Prerequisites
- Java 11 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/VIHAN-droid/University-ERP-project.git
cd University-ERP-project
```

2. **Set up MySQL databases**
```bash
mysql -u root -p
```
```sql
-- Create databases
CREATE DATABASE auth_db;
CREATE DATABASE erp_db;

-- Run schema files
SOURCE database/auth_db_schema.sql;
SOURCE database/erp_db_schema.sql;

-- Load seed data
SOURCE database/auth_db_seed.sql;
SOURCE database/erp_db_seed.sql;
```

3. **Configure database connection**

Edit `src/main/resources/application.properties`:
```properties
auth.db.url=jdbc:mysql://localhost:3306/auth_db
auth.db.username=root
auth.db.password=your_password

erp.db.url=jdbc:mysql://localhost:3306/erp_db
erp.db.username=root
erp.db.password=your_password
```

4. **Build the project**
```bash
mvn clean package
```

5. **Run the application**
```bash
java -jar target/AP-proj-ERP-1.0.0-jar-with-dependencies.jar
```

### Default Credentials

All default accounts use password: `Supersid05`

| Username | Role       | Description                    |
|----------|-----------|--------------------------------|
| admin1   | ADMIN     | Full system access             |
| inst1    | INSTRUCTOR| Computer Science instructor    |
| inst2    | INSTRUCTOR| Mathematics instructor         |
| stu1     | STUDENT   | CS student, Year 3             |
| stu2     | STUDENT   | CS student, Year 3             |
| stu3     | STUDENT   | EE student, Year 2             |

## 📊 Features Deep Dive

### Grade Calculation System
- Component-based grading (Quiz, Midterm, Final, etc.)
- Automatic percentage calculation per component
- Weighted average computation (weightages must sum to 100%)
- Letter grade mapping:
  - A+ (95-100%), A (90-95%), A- (85-90%)
  - B+ (80-85%), B (75-80%), B- (70-75%)
  - C+ (65-70%), C (60-65%), C- (55-60%)
  - D (50-55%), F (<50%)
- CGPA calculation on 10-point scale

### Course Registration Logic
- Prevents duplicate enrollments in same course
- Enforces section capacity limits
- Checks for conflicting schedules
- Respects add/drop period restrictions
- Maintains enrollment counts automatically

### Access Control System
- **Maintenance Mode**: Disables all write operations except for admins
- **Add/Drop Period**: Controls student registration/drop capabilities
- **Role-Based Permissions**: Granular access control per user role
- **Session Management**: Tracks current user throughout application lifecycle

## 🎨 UI Features

- **Modern Dark Theme**: Eye-friendly design with gradient accents
- **Responsive Tables**: Sortable, selectable data grids
- **Status Banners**: Visual indicators for system modes
- **Gradient Effects**: Polished login and header sections
- **Timetable View**: Weekly schedule with color-coded courses
- **Export Options**: PDF and CSV generation for reports

## 📁 Project Structure

```
src/main/java/edu/univ/erp/
├── access/              # Access control and permissions
├── auth/                # Authentication and session management
├── data/                # DAO layer for database operations
├── domain/              # Entity classes (Student, Course, Grade, etc.)
├── service/             # Business logic layer
├── ui/
│   ├── admin/          # Admin dashboard and components
│   ├── instructor/     # Instructor dashboard and components
│   ├── student/        # Student dashboard and components
│   └── common/         # Shared UI components (Login, Theme, etc.)
└── util/                # Utilities (DatabaseManager, MessageUtil)

database/
├── auth_db_schema.sql   # Authentication database schema
├── auth_db_seed.sql     # Default users and data
├── erp_db_schema.sql    # ERP database schema
└── erp_db_seed.sql      # Sample courses and enrollments

src/main/resources/
├── application.properties  # Configuration
└── logback.xml            # Logging configuration
```

## 🔧 Configuration

### Database Connection Pool
```properties
db.pool.maximumPoolSize=10     # Maximum concurrent connections
db.pool.minimumIdle=5          # Idle connections to maintain
db.pool.connectionTimeout=30000 # Connection timeout (ms)
```

### Logging
- Console output: `HH:mm:ss` format
- File output: `logs/erp-application.log`
- Rolling policy: Daily rotation, 30-day retention

## 🛡️ Security Features

1. **Password Security**
   - BCrypt hashing with automatic salt generation
   - Minimum 8 characters with uppercase, lowercase, and digit requirements
   - Password history tracking

2. **Account Protection**
   - Automatic lockout after 5 failed attempts
   - Warning messages for remaining attempts
   - Admin-controlled unlock functionality

3. **SQL Injection Prevention**
   - Prepared statements for all database queries
   - Input validation and sanitization

4. **Session Security**
   - Single sign-on enforcement
   - Automatic session cleanup on logout
   - Role verification on sensitive operations

## 🐛 Known Limitations

- Single-user application (no concurrent multi-user support)
- No email notifications for password resets
- Fixed semester/year in some operations
- Time slot parsing assumes specific formats
- No audit logging for admin operations

## 🚀 Future Enhancements

- [ ] Multi-user concurrent access with proper locking
- [ ] Email integration for notifications
- [ ] Attendance tracking module
- [ ] Fee management system
- [ ] Academic calendar integration
- [ ] Report card generation
- [ ] Mobile-responsive web interface
- [ ] REST API for external integrations
- [ ] Real-time notifications using WebSocket

## 📝 Development Notes

### Building from Source
```bash
# Compile only
mvn compile

# Run tests
mvn test

# Package without dependencies
mvn package

# Create executable JAR with dependencies
mvn clean package assembly:single
```

### Database Backup
The application includes built-in backup functionality:
1. Navigate to Admin Dashboard → System Settings
2. Click "Create Backup"
3. Backups are stored in `backups/` directory
4. Restore using "Restore Selected" button

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
