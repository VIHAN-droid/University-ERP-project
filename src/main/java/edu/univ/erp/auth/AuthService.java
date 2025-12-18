package edu.univ.erp.auth;

import edu.univ.erp.domain.User;
import edu.univ.erp.util.DatabaseManager;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static AuthService instance;

    private AuthService() {}

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public User login(String username, String password) {
        LoginResult result = loginWithResult(username, password);
        return result.isSuccess() ? result.getUser() : null;
    }

    public LoginResult loginWithResult(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            logger.warn("Login attempt with empty credentials");
            return LoginResult.failure("Please enter username and password.");
        }

        try (Connection conn = DatabaseManager.getInstance().getAuthConnection()) {
            String sql = "SELECT user_id, username, role, password_hash, status, failed_login_attempts " +
                        "FROM users_auth WHERE username = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username.trim());
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    User user = mapResultSetToUser(rs);

                    if (user.getStatus() == User.UserStatus.LOCKED) {
                        logger.warn("Login attempt for locked account: {}", username);
                        return LoginResult.locked("Your account has been locked due to too many failed login attempts. Please contact an administrator.");
                    }

                    if (user.getStatus() == User.UserStatus.INACTIVE) {
                        logger.warn("Login attempt for inactive account: {}", username);
                        return LoginResult.failure("Your account is inactive. Please contact an administrator.");
                    }

                    logger.info("Verifying password for user: {}", username.trim());
                    boolean passwordMatch = BCrypt.checkpw(password.trim(), user.getPasswordHash());

                    if (passwordMatch) {

                        resetFailedAttempts(conn, user.getUserId());
                        updateLastLogin(conn, user.getUserId());
                        logger.info("Successful login for user: {}", username.trim());
                        return LoginResult.success(user);
                    } else {

                        incrementFailedAttempts(conn, user.getUserId());
                        int newAttempts = user.getFailedLoginAttempts() + 1;
                        logger.warn("Failed login attempt for user: {}", username.trim());

                        if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                            return LoginResult.locked("Your account has been locked due to too many failed login attempts. Please contact an administrator.");
                        }

                        int remaining = MAX_FAILED_ATTEMPTS - newAttempts;
                        if (remaining <= 2) {
                            return LoginResult.failure("Incorrect username or password. Warning: " + remaining + " attempt(s) remaining before account is locked.");
                        }
                        return LoginResult.failure("Incorrect username or password.");
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error during login", e);
            return LoginResult.failure("Login error. Please try again.");
        }

        return LoginResult.failure("Incorrect username or password.");
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            logger.warn("Password change attempt with invalid new password");
            return false;
        }

        try (Connection conn = DatabaseManager.getInstance().getAuthConnection()) {

            String sql = "SELECT password_hash FROM users_auth WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String currentHash = rs.getString("password_hash");
                    if (!BCrypt.checkpw(oldPassword.trim(), currentHash)) {
                        logger.warn("Password change failed - incorrect old password for user: {}", userId);
                        return false;
                    }
                }
            }

            String updateSql = "UPDATE users_auth SET password_hash = ?, updated_at = ? WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                String newHash = BCrypt.hashpw(newPassword.trim(), BCrypt.gensalt());
                pstmt.setString(1, newHash);
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(3, userId);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    logger.info("Password changed successfully for user: {}", userId);

                    storePasswordHistory(conn, userId, newHash);
                    return true;
                }
            }
        } catch (SQLException e) {
            logger.error("Error changing password", e);
        }

        return false;
    }

    public boolean adminResetPassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            logger.warn("Admin password reset attempt with invalid new password");
            return false;
        }

        try (Connection conn = DatabaseManager.getInstance().getAuthConnection()) {

            String updateSql = "UPDATE users_auth SET password_hash = ?, updated_at = ? WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                String newHash = BCrypt.hashpw(newPassword.trim(), BCrypt.gensalt());
                pstmt.setString(1, newHash);
                pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setInt(3, userId);

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    logger.info("Admin password reset successful for user: {}", userId);

                    storePasswordHistory(conn, userId, newHash);
                    return true;
                }
            }
        } catch (SQLException e) {
            logger.error("Error during admin password reset", e);
        }

        return false;
    }

    private void incrementFailedAttempts(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE users_auth SET failed_login_attempts = failed_login_attempts + 1, " +
                    "status = CASE WHEN failed_login_attempts + 1 >= ? THEN 'LOCKED' ELSE status END " +
                    "WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, MAX_FAILED_ATTEMPTS);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    private void resetFailedAttempts(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE users_auth SET failed_login_attempts = 0 WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    private void updateLastLogin(Connection conn, int userId) throws SQLException {
        String sql = "UPDATE users_auth SET last_login = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    private void storePasswordHistory(Connection conn, int userId, String passwordHash) {
        String sql = "INSERT INTO password_history (user_id, password_hash) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, passwordHash);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error storing password history", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setRole(User.UserRole.valueOf(rs.getString("role")));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setStatus(User.UserStatus.valueOf(rs.getString("status")));
        user.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
        return user;
    }
}
