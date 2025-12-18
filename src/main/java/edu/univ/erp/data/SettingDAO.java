package edu.univ.erp.data;

import edu.univ.erp.domain.Setting;
import edu.univ.erp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class SettingDAO {
    private static final Logger logger = LoggerFactory.getLogger(SettingDAO.class);

    public String getValue(String key) {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("setting_value");
            }
        } catch (SQLException e) {
            logger.error("Error fetching setting", e);
        }
        return null;
    }

    public boolean setValue(String key, String value) {
        String sql = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE setting_value = ?";
        try (Connection conn = DatabaseManager.getInstance().getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.setString(3, value);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error setting value", e);
        }
        return false;
    }

    public boolean getMaintenanceMode() {
        String value = getValue("maintenance_mode");
        return "true".equalsIgnoreCase(value);
    }

    public boolean setMaintenanceMode(boolean enabled) {
        return setValue("maintenance_mode", String.valueOf(enabled));
    }

    public boolean isAddDropEnabled() {
        String value = getValue("add_drop_enabled");

        return value == null || "true".equalsIgnoreCase(value);
    }

    public boolean setAddDropEnabled(boolean enabled) {
        return setValue("add_drop_enabled", String.valueOf(enabled));
    }
}
