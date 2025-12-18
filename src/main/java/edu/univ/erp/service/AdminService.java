package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;
import edu.univ.erp.util.DatabaseManager;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final StudentDAO studentDAO;
    private final InstructorDAO instructorDAO;
    private final CourseDAO courseDAO;
    private final SectionDAO sectionDAO;
    private final SettingDAO settingDAO;
    private final UserDAO userDAO;
    private final AccessControl accessControl;

    public AdminService() {
        this.studentDAO = new StudentDAO();
        this.instructorDAO = new InstructorDAO();
        this.courseDAO = new CourseDAO();
        this.sectionDAO = new SectionDAO();
        this.settingDAO = new SettingDAO();
        this.userDAO = new UserDAO();
        this.accessControl = AccessControl.getInstance();
    }

    public String createUser(String username, String password, User.UserRole role,
                            String additionalInfo1, String additionalInfo2) {
        if (!accessControl.isAdmin()) return accessControl.getPermissionDeniedMessage();

        if (isUsernameTaken(username)) {
            return "Username already exists.";
        }

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection conn = DatabaseManager.getInstance().getAuthConnection()) {
            conn.setAutoCommit(false);
            try {

                String userSql = "INSERT INTO users_auth (username, role, password_hash, status) VALUES (?, ?, ?, 'ACTIVE')";
                int userId = -1;

                try (PreparedStatement pstmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, role.name());
                    pstmt.setString(3, passwordHash);
                    pstmt.executeUpdate();

                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            userId = rs.getInt(1);
                        }
                    }
                }

                if (userId == -1) {
                    conn.rollback();
                    return "Failed to create user record.";
                }

                try (Connection erpConn = DatabaseManager.getInstance().getErpConnection()) {
                    erpConn.setAutoCommit(false);
                    try {
                        if (role == User.UserRole.STUDENT) {
                            String studSql = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
                            try (PreparedStatement pstmt = erpConn.prepareStatement(studSql)) {
                                pstmt.setInt(1, userId);
                                pstmt.setString(2, additionalInfo1);
                                pstmt.setString(3, "Undeclared");
                                pstmt.setInt(4, Integer.parseInt(additionalInfo2));
                                pstmt.executeUpdate();
                            }
                        } else if (role == User.UserRole.INSTRUCTOR) {
                            String instSql = "INSERT INTO instructors (user_id, employee_id, department) VALUES (?, ?, ?)";
                            try (PreparedStatement pstmt = erpConn.prepareStatement(instSql)) {
                                pstmt.setInt(1, userId);
                                pstmt.setString(2, additionalInfo1);
                                pstmt.setString(3, additionalInfo2);
                                pstmt.executeUpdate();
                            }
                        }
                        erpConn.commit();
                    } catch (Exception e) {
                        erpConn.rollback();
                        throw e;
                    }
                }

                conn.commit();
                logger.info("User created: {}", username);
                return null;

            } catch (Exception e) {
                conn.rollback();
                logger.error("Transaction failed", e);
                return "Error creating user: " + e.getMessage();
            }
        } catch (SQLException e) {
            logger.error("Database error", e);
            return "Database error.";
        }
    }

    public List<User> getAllUsers() {
        if (!accessControl.isAdmin()) return List.of();
        return userDAO.getAll();
    }

    public String updateUserStatus(int userId, User.UserStatus status) {
        if (!accessControl.isAdmin()) return accessControl.getPermissionDeniedMessage();
        if (userDAO.updateUserStatus(userId, status)) return null;
        return "Failed to update status.";
    }

    private boolean isUsernameTaken(String username) {
        String sql = "SELECT 1 FROM users_auth WHERE username = ?";
        try (Connection conn = DatabaseManager.getInstance().getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return true;
        }
    }

    public String createCourse(String code, String title, int credits, String description) {
        if (!accessControl.isAdmin()) return accessControl.getPermissionDeniedMessage();

        Course course = new Course(code, title, credits);
        course.setDescription(description);

        if (courseDAO.create(course)) {
            logger.info("Course created: {}", code);
            return null;
        }
        return "Failed to create course (Code might be duplicate).";
    }

    public List<Course> getAllCourses() {
        return courseDAO.getAll();
    }

    public String updateCourse(Course course) {
        if (!accessControl.isAdmin()) return accessControl.getPermissionDeniedMessage();
        if (courseDAO.update(course)) return null;
        return "Failed to update course.";
    }

    public String createSection(int courseId, Integer instructorId, String sectionCode,
                               String dayTime, String room, int capacity,
                               String semester, int year) {
        if (!accessControl.isAdmin()) return accessControl.getPermissionDeniedMessage();

        if (sectionCode == null || sectionCode.trim().isEmpty()) {
            return "Section code is required (e.g., A, B, or C).";
        }

        if (capacity <= 0) {
            return "Capacity must be a positive number greater than 0.";
        }

        if (dayTime == null || dayTime.trim().isEmpty()) {
            return "Day/Time is required (e.g., 'Mon/Wed 10:00-11:30').";
        }

        if (room == null || room.trim().isEmpty()) {
            return "Room is required (e.g., 'Room 101' or 'Online').";
        }

        if (semester == null || semester.trim().isEmpty()) {
            return "Semester is required (e.g., 'Fall' or 'Spring').";
        }

        if (year < 2000 || year > 2100) {
            return "Please enter a valid year (between 2000 and 2100).";
        }

        Section section = new Section(courseId, sectionCode.trim(), capacity, semester.trim(), year);
        section.setInstructorId(instructorId);
        section.setDayTime(dayTime.trim());
        section.setRoom(room.trim());

        if (sectionDAO.create(section)) {
            logger.info("Section created: {}-{}", courseId, sectionCode);
            return null;
        }
        return "Failed to create section. A section with this code may already exist for this course/semester.";
    }

    public List<Section> getAllSections() {
        return sectionDAO.getAll();
    }

    public String updateSection(Section section) {
        if (!accessControl.isAdmin()) return accessControl.getPermissionDeniedMessage();

        if (section.getCapacity() <= 0) {
            return "Capacity must be a positive number.";
        }

        if (sectionDAO.update(section)) return null;
        return "Failed to update section.";
    }

    public String deleteSection(int sectionId) {
        if (!accessControl.isAdmin()) return accessControl.getPermissionDeniedMessage();

        Section section = sectionDAO.getById(sectionId);
        if (section == null) {
            return "Section not found.";
        }

        if (section.getEnrolledCount() > 0) {
            return "Cannot delete section with enrolled students. Please remove all enrollments first or wait until the section is completed.";
        }

        if (sectionDAO.delete(sectionId)) {
            logger.info("Section deleted: {}", sectionId);
            return null;
        }
        return "Failed to delete section.";
    }

    public List<Instructor> getAllInstructors() {
        return instructorDAO.getAll();
    }

    public String toggleMaintenanceMode(boolean enabled) {
        if (!accessControl.isAdmin()) return accessControl.getPermissionDeniedMessage();

        if (settingDAO.setMaintenanceMode(enabled)) {
            logger.info("Maintenance mode set to: {}", enabled);
            return null;
        }
        return "Failed to update maintenance mode.";
    }

    public boolean isMaintenanceModeEnabled() {
        return settingDAO.getMaintenanceMode();
    }

    public String toggleAddDropPeriod(boolean enabled) {
        if (!accessControl.isAdmin()) return accessControl.getPermissionDeniedMessage();

        if (settingDAO.setAddDropEnabled(enabled)) {
            logger.info("Add/Drop period set to: {}", enabled);
            return null;
        }
        return "Failed to update add/drop period setting.";
    }

    public boolean isAddDropEnabled() {
        return settingDAO.isAddDropEnabled();
    }
}