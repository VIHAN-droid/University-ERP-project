package edu.univ.erp.data;

import edu.univ.erp.domain.Course;
import edu.univ.erp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {
    private static final Logger logger = LoggerFactory.getLogger(CourseDAO.class);

    public Course getById(int courseId) {
        String sql = "SELECT * FROM courses WHERE course_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCourse(rs);
            }
        } catch (SQLException e) {
            logger.error("Error fetching course", e);
        }
        return null;
    }

    public Course getByCode(String code) {
        String sql = "SELECT * FROM courses WHERE code = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCourse(rs);
            }
        } catch (SQLException e) {
            logger.error("Error fetching course by code", e);
        }
        return null;
    }

    public boolean create(Course course) {
        String sql = "INSERT INTO courses (code, title, credits, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getCode());
            pstmt.setString(2, course.getTitle());
            pstmt.setInt(3, course.getCredits());
            pstmt.setString(4, course.getDescription());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating course", e);
        }
        return false;
    }

    public boolean update(Course course) {
        String sql = "UPDATE courses SET code=?, title=?, credits=?, description=?, updated_at=NOW() WHERE course_id=?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getCode());
            pstmt.setString(2, course.getTitle());
            pstmt.setInt(3, course.getCredits());
            pstmt.setString(4, course.getDescription());
            pstmt.setInt(5, course.getCourseId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating course", e);
        }
        return false;
    }

    public List<Course> getAll() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY code";

        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching courses", e);
        }
        return courses;
    }

    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseId(rs.getInt("course_id"));
        course.setCode(rs.getString("code"));
        course.setTitle(rs.getString("title"));
        course.setCredits(rs.getInt("credits"));
        course.setDescription(rs.getString("description"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            course.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            course.setUpdatedAt(updated.toLocalDateTime());
        }

        return course;
    }
}