package edu.univ.erp.data;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentDAO.class);

    public List<Enrollment> getByStudent(int studentId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, c.code as course_code, c.title as course_title, " +
                    "s.section_code, s.semester, s.year, s.day_time, s.room " +
                    "FROM enrollments e " +
                    "JOIN sections s ON e.section_id = s.section_id " +
                    "JOIN courses c ON s.course_id = c.course_id " +
                    "WHERE e.student_id = ? AND e.status = 'ENROLLED' " +
                    "ORDER BY c.code";

        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollment(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching student enrollments", e);
        }
        return enrollments;
    }

    public List<Enrollment> getBySection(int sectionId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT e.*, c.code as course_code, c.title as course_title, " +
                    "s.section_code, s.semester, s.year, s.day_time, s.room, " +
                    "st.roll_no as student_roll_no, st.program as student_program " +
                    "FROM enrollments e " +
                    "JOIN sections s ON e.section_id = s.section_id " +
                    "JOIN courses c ON s.course_id = c.course_id " +
                    "JOIN students st ON e.student_id = st.student_id " +
                    "WHERE e.section_id = ? AND e.status = 'ENROLLED' " +
                    "ORDER BY st.roll_no";

        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sectionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                enrollments.add(mapResultSetToEnrollmentWithStudent(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching section enrollments", e);
        }
        return enrollments;
    }

    public Enrollment getById(int enrollmentId) {
        String sql = "SELECT e.*, c.code as course_code, c.title as course_title, " +
                    "s.section_code, s.semester, s.year, s.day_time, s.room " +
                    "FROM enrollments e " +
                    "JOIN sections s ON e.section_id = s.section_id " +
                    "JOIN courses c ON s.course_id = c.course_id " +
                    "WHERE e.enrollment_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEnrollment(rs);
            }
        } catch (SQLException e) {
            logger.error("Error fetching enrollment", e);
        }
        return null;
    }

    public boolean exists(int studentId, int sectionId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND section_id = ? AND status = 'ENROLLED'";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, sectionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking enrollment existence", e);
        }
        return false;
    }

    public boolean existsForCourse(int studentId, int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments e " +
                    "JOIN sections s ON e.section_id = s.section_id " +
                    "WHERE e.student_id = ? AND s.course_id = ? AND e.status = 'ENROLLED'";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking course enrollment existence", e);
        }
        return false;
    }

    public String getExistingSectionCode(int studentId, int courseId) {
        String sql = "SELECT s.section_code FROM enrollments e " +
                    "JOIN sections s ON e.section_id = s.section_id " +
                    "WHERE e.student_id = ? AND s.course_id = ? AND e.status = 'ENROLLED' " +
                    "LIMIT 1";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("section_code");
            }
        } catch (SQLException e) {
            logger.error("Error getting existing section code", e);
        }
        return null;
    }

    public boolean create(Enrollment enrollment) {
        String sql = "INSERT INTO enrollments (student_id, section_id, status, drop_deadline) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, enrollment.getStudentId());
            pstmt.setInt(2, enrollment.getSectionId());
            pstmt.setString(3, enrollment.getStatus().name());

            if (enrollment.getDropDeadline() != null) {
                pstmt.setTimestamp(4, Timestamp.valueOf(enrollment.getDropDeadline()));
            } else {
                pstmt.setNull(4, Types.TIMESTAMP);
            }

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    enrollment.setEnrollmentId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating enrollment", e);
        }
        return false;
    }

    public boolean drop(int enrollmentId) {
        String sql = "UPDATE enrollments SET status = 'DROPPED' WHERE enrollment_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error dropping enrollment", e);
        }
        return false;
    }

    private Enrollment mapResultSetToEnrollment(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
        enrollment.setStudentId(rs.getInt("student_id"));
        enrollment.setSectionId(rs.getInt("section_id"));
        enrollment.setStatus(Enrollment.EnrollmentStatus.valueOf(rs.getString("status")));

        Timestamp enrolled = rs.getTimestamp("enrolled_date");
        if (enrolled != null) {
            enrollment.setEnrolledDate(enrolled.toLocalDateTime());
        }

        Timestamp deadline = rs.getTimestamp("drop_deadline");
        if (deadline != null) {
            enrollment.setDropDeadline(deadline.toLocalDateTime());
        }

        enrollment.setCourseCode(rs.getString("course_code"));
        enrollment.setCourseTitle(rs.getString("course_title"));
        enrollment.setSectionCode(rs.getString("section_code"));
        enrollment.setSemester(rs.getString("semester"));
        enrollment.setYear(rs.getInt("year"));
        enrollment.setDayTime(rs.getString("day_time"));
        enrollment.setRoom(rs.getString("room"));

        return enrollment;
    }

    private Enrollment mapResultSetToEnrollmentWithStudent(ResultSet rs) throws SQLException {
        Enrollment enrollment = mapResultSetToEnrollment(rs);

        try {
            enrollment.setStudentRollNo(rs.getString("student_roll_no"));
            enrollment.setStudentProgram(rs.getString("student_program"));
        } catch (SQLException e) {

        }

        return enrollment;
    }
}
