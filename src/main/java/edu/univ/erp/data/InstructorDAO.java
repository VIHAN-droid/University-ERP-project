package edu.univ.erp.data;

import edu.univ.erp.domain.Instructor;
import edu.univ.erp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstructorDAO {
    private static final Logger logger = LoggerFactory.getLogger(InstructorDAO.class);

    public Instructor getByUserId(int userId) {
        String sql = "SELECT * FROM instructors WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToInstructor(rs);
            }
        } catch (SQLException e) {
            logger.error("Error fetching instructor", e);
        }
        return null;
    }

    public Instructor getById(int instructorId) {
        String sql = "SELECT * FROM instructors WHERE instructor_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, instructorId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToInstructor(rs);
            }
        } catch (SQLException e) {
            logger.error("Error fetching instructor", e);
        }
        return null;
    }

    public boolean create(Instructor instructor) {
        String sql = "INSERT INTO instructors (user_id, employee_id, department, email, phone) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, instructor.getUserId());
            pstmt.setString(2, instructor.getEmployeeId());
            pstmt.setString(3, instructor.getDepartment());
            pstmt.setString(4, instructor.getEmail());
            pstmt.setString(5, instructor.getPhone());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    instructor.setInstructorId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating instructor", e);
        }
        return false;
    }

    public List<Instructor> getAll() {
        List<Instructor> instructors = new ArrayList<>();
        String sql = "SELECT * FROM instructors ORDER BY employee_id";

        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                instructors.add(mapResultSetToInstructor(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching instructors", e);
        }
        return instructors;
    }

    private Instructor mapResultSetToInstructor(ResultSet rs) throws SQLException {
        Instructor instructor = new Instructor();
        instructor.setInstructorId(rs.getInt("instructor_id"));
        instructor.setUserId(rs.getInt("user_id"));
        instructor.setEmployeeId(rs.getString("employee_id"));
        instructor.setDepartment(rs.getString("department"));
        instructor.setEmail(rs.getString("email"));
        instructor.setPhone(rs.getString("phone"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            instructor.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            instructor.setUpdatedAt(updated.toLocalDateTime());
        }

        return instructor;
    }
}
