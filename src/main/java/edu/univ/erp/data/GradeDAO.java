package edu.univ.erp.data;

import edu.univ.erp.domain.Grade;
import edu.univ.erp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradeDAO {
    private static final Logger logger = LoggerFactory.getLogger(GradeDAO.class);

    public List<Grade> getByEnrollment(int enrollmentId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT * FROM grades WHERE enrollment_id = ? ORDER BY component";

        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching grades", e);
        }
        return grades;
    }

    public boolean create(Grade grade) {
        String sql = "INSERT INTO grades (enrollment_id, component, score, max_score, weightage, final_grade) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, grade.getEnrollmentId());
            pstmt.setString(2, grade.getComponent());
            pstmt.setBigDecimal(3, grade.getScore());
            pstmt.setBigDecimal(4, grade.getMaxScore());
            pstmt.setBigDecimal(5, grade.getWeightage());
            pstmt.setString(6, grade.getFinalGrade());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    grade.setGradeId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating grade", e);
        }
        return false;
    }

    public boolean update(Grade grade) {
        String sql = "UPDATE grades SET score = ?, max_score = ?, weightage = ?, final_grade = ? WHERE grade_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, grade.getScore());
            pstmt.setBigDecimal(2, grade.getMaxScore());
            pstmt.setBigDecimal(3, grade.getWeightage());
            pstmt.setString(4, grade.getFinalGrade());
            pstmt.setInt(5, grade.getGradeId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating grade", e);
        }
        return false;
    }

    public boolean existsByComponent(int enrollmentId, String component) {
        String sql = "SELECT COUNT(*) FROM grades WHERE enrollment_id = ? AND LOWER(component) = LOWER(?)";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);
            pstmt.setString(2, component);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking grade component existence", e);
        }
        return false;
    }

    public java.math.BigDecimal getTotalWeightage(int enrollmentId) {
        String sql = "SELECT COALESCE(SUM(weightage), 0) as total FROM grades WHERE enrollment_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            logger.error("Error calculating total weightage", e);
        }
        return java.math.BigDecimal.ZERO;
    }

    public java.math.BigDecimal getTotalWeightageExcluding(int enrollmentId, int excludeGradeId) {
        String sql = "SELECT COALESCE(SUM(weightage), 0) as total FROM grades WHERE enrollment_id = ? AND grade_id != ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);
            pstmt.setInt(2, excludeGradeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            logger.error("Error calculating total weightage excluding grade", e);
        }
        return java.math.BigDecimal.ZERO;
    }

    public Grade getById(int gradeId) {
        String sql = "SELECT * FROM grades WHERE grade_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gradeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToGrade(rs);
            }
        } catch (SQLException e) {
            logger.error("Error fetching grade by ID", e);
        }
        return null;
    }

    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setGradeId(rs.getInt("grade_id"));
        grade.setEnrollmentId(rs.getInt("enrollment_id"));
        grade.setComponent(rs.getString("component"));
        grade.setScore(rs.getBigDecimal("score"));
        grade.setMaxScore(rs.getBigDecimal("max_score"));
        grade.setWeightage(rs.getBigDecimal("weightage"));
        grade.setFinalGrade(rs.getString("final_grade"));
        return grade;
    }
}
