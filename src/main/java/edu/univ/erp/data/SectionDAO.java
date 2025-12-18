package edu.univ.erp.data;

import edu.univ.erp.domain.Section;
import edu.univ.erp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SectionDAO {
    private static final Logger logger = LoggerFactory.getLogger(SectionDAO.class);

    public Section getById(int sectionId) {
        String sql = "SELECT s.*, c.code as course_code, c.title as course_title, c.credits as course_credits, " +
                    "COALESCE(u.username, 'TBA') as instructor_name " +
                    "FROM sections s " +
                    "JOIN courses c ON s.course_id = c.course_id " +
                    "LEFT JOIN instructors i ON s.instructor_id = i.instructor_id " +
                    "LEFT JOIN auth_db.users_auth u ON i.user_id = u.user_id " +
                    "WHERE s.section_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sectionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToSection(rs);
            }
        } catch (SQLException e) {
            logger.error("Error fetching section", e);
        }
        return null;
    }

    public List<Section> getAll() {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT s.*, c.code as course_code, c.title as course_title, c.credits as course_credits, " +
                "COALESCE(u.username, 'TBA') as instructor_name " +
                "FROM sections s " +
                "JOIN courses c ON s.course_id = c.course_id " +
                "LEFT JOIN instructors i ON s.instructor_id = i.instructor_id " +
                "LEFT JOIN auth_db.users_auth u ON i.user_id = u.user_id " +
                "ORDER BY s.year DESC, s.semester, c.code";

        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                sections.add(mapResultSetToSection(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all sections", e);
        }
        return sections;
    }

    public List<Section> getByInstructor(int instructorId) {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT s.*, c.code as course_code, c.title as course_title, c.credits as course_credits, " +
                    "COALESCE(u.username, 'Me') as instructor_name " +
                    "FROM sections s " +
                    "JOIN courses c ON s.course_id = c.course_id " +
                    "LEFT JOIN instructors i ON s.instructor_id = i.instructor_id " +
                    "LEFT JOIN auth_db.users_auth u ON i.user_id = u.user_id " +
                    "WHERE s.instructor_id = ? " +
                    "ORDER BY s.year DESC, s.semester";

        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, instructorId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                sections.add(mapResultSetToSection(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching instructor sections", e);
        }
        return sections;
    }

    public boolean create(Section section) {
        String sql = "INSERT INTO sections (course_id, instructor_id, section_code, day_time, room, capacity, semester, year, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, section.getCourseId());
            if (section.getInstructorId() != null) {
                pstmt.setInt(2, section.getInstructorId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setString(3, section.getSectionCode());
            pstmt.setString(4, section.getDayTime());
            pstmt.setString(5, section.getRoom());
            pstmt.setInt(6, section.getCapacity());
            pstmt.setString(7, section.getSemester());
            pstmt.setInt(8, section.getYear());
            pstmt.setString(9, Section.SectionStatus.ACTIVE.name());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating section", e);
        }
        return false;
    }

    public boolean update(Section section) {
        String sql = "UPDATE sections SET instructor_id = ?, section_code = ?, day_time = ?, room = ?, " +
                "capacity = ?, semester = ?, year = ?, status = ? WHERE section_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (section.getInstructorId() != null && section.getInstructorId() > 0) {
                pstmt.setInt(1, section.getInstructorId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setString(2, section.getSectionCode());
            pstmt.setString(3, section.getDayTime());
            pstmt.setString(4, section.getRoom());
            pstmt.setInt(5, section.getCapacity());
            pstmt.setString(6, section.getSemester());
            pstmt.setInt(7, section.getYear());
            pstmt.setString(8, section.getStatus() != null ? section.getStatus().name() : "ACTIVE");
            pstmt.setInt(9, section.getSectionId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating section", e);
        }
        return false;
    }

    public boolean updateEnrolledCount(int sectionId, int delta) {
        String sql = "UPDATE sections SET enrolled_count = enrolled_count + ? WHERE section_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, delta);
            pstmt.setInt(2, sectionId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating enrolled count", e);
        }
        return false;
    }

    public boolean delete(int sectionId) {
        String sql = "DELETE FROM sections WHERE section_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sectionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting section", e);
        }
        return false;
    }

    private Section mapResultSetToSection(ResultSet rs) throws SQLException {
        Section section = new Section();
        section.setSectionId(rs.getInt("section_id"));
        section.setCourseId(rs.getInt("course_id"));

        int instructorId = rs.getInt("instructor_id");
        if (!rs.wasNull()) {
            section.setInstructorId(instructorId);
        }

        section.setSectionCode(rs.getString("section_code"));
        section.setDayTime(rs.getString("day_time"));
        section.setRoom(rs.getString("room"));
        section.setCapacity(rs.getInt("capacity"));
        section.setEnrolledCount(rs.getInt("enrolled_count"));
        section.setSemester(rs.getString("semester"));
        section.setYear(rs.getInt("year"));
        section.setStatus(Section.SectionStatus.valueOf(rs.getString("status")));

        try {
            section.setCourseCode(rs.getString("course_code"));
            section.setCourseTitle(rs.getString("course_title"));
            section.setCourseCredits(rs.getInt("course_credits"));
            section.setInstructorName(rs.getString("instructor_name"));
        } catch (SQLException e) {

        }

        return section;
    }
}