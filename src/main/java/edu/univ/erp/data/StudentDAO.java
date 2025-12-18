package edu.univ.erp.data;

import edu.univ.erp.domain.Student;
import edu.univ.erp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    private static final Logger logger = LoggerFactory.getLogger(StudentDAO.class);

    public Student getByUserId(int userId) {

        String sql = "SELECT * FROM students WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToStudent(rs);
            }
        } catch (SQLException e) {
            logger.error("Error fetching student by user ID", e);
        }
        return null;
    }

    public Student getById(int studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToStudent(rs);
            }
        } catch (SQLException e) {
            logger.error("Error fetching student by ID", e);
        }
        return null;
    }

    public boolean create(Student student) {
        String sql = "INSERT INTO students (user_id, roll_no, program, year, email, phone) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, student.getUserId());
            pstmt.setString(2, student.getRollNo());
            pstmt.setString(3, student.getProgram());
            pstmt.setInt(4, student.getYear());
            pstmt.setString(5, student.getEmail());
            pstmt.setString(6, student.getPhone());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    student.setStudentId(rs.getInt(1));
                }
                logger.info("Student created: {}", student.getRollNo());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating student", e);
        }
        return false;
    }

    public List<Student> getAll() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY roll_no";

        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all students", e);
        }
        return students;
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStudentId(rs.getInt("student_id"));
        student.setUserId(rs.getInt("user_id"));
        student.setRollNo(rs.getString("roll_no"));
        student.setProgram(rs.getString("program"));
        student.setYear(rs.getInt("year"));
        student.setEmail(rs.getString("email"));
        student.setPhone(rs.getString("phone"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            student.setCreatedAt(created.toLocalDateTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) {
            student.setUpdatedAt(updated.toLocalDateTime());
        }

        return student;
    }
}
