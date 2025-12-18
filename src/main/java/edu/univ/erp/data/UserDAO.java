package edu.univ.erp.data;

import edu.univ.erp.domain.User;
import edu.univ.erp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, role, status, last_login, created_at FROM users_auth ORDER BY user_id";

        try (Connection conn = DatabaseManager.getInstance().getAuthConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(User.UserRole.valueOf(rs.getString("role")));
                user.setStatus(User.UserStatus.valueOf(rs.getString("status")));

                Timestamp lastLogin = rs.getTimestamp("last_login");
                if (lastLogin != null) user.setLastLogin(lastLogin.toLocalDateTime());

                Timestamp created = rs.getTimestamp("created_at");
                if (created != null) user.setCreatedAt(created.toLocalDateTime());

                users.add(user);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all users", e);
        }
        return users;
    }

    public boolean updateUserStatus(int userId, User.UserStatus status) {

        String sql = "UPDATE users_auth SET status = ?, failed_login_attempts = CASE WHEN ? = 'ACTIVE' THEN 0 ELSE failed_login_attempts END WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            pstmt.setString(2, status.name());
            pstmt.setInt(3, userId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating user status", e);
        }
        return false;
    }

    public int create(User user) {

        return -1;
    }
}