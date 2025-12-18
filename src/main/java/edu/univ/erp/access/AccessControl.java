package edu.univ.erp.access;

import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.SettingDAO;
import edu.univ.erp.domain.User;

public class AccessControl {
    private static AccessControl instance;
    private final SettingDAO settingDAO;

    private AccessControl() {
        this.settingDAO = new SettingDAO();
    }

    public static synchronized AccessControl getInstance() {
        if (instance == null) {
            instance = new AccessControl();
        }
        return instance;
    }

    public boolean isMaintenanceModeEnabled() {
        return settingDAO.getMaintenanceMode();
    }

    public boolean isAddDropEnabled() {
        return settingDAO.isAddDropEnabled();
    }

    public boolean canModifyData() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        if (currentUser.getRole() == User.UserRole.ADMIN) {
            return true;
        }

        if (isMaintenanceModeEnabled()) {
            return false;
        }

        return true;
    }

    public boolean canStudentAddDropCourses() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        if (currentUser.getRole() == User.UserRole.ADMIN) {
            return true;
        }

        if (!isAddDropEnabled()) {
            return false;
        }

        if (isMaintenanceModeEnabled()) {
            return false;
        }

        return true;
    }

    public boolean isAdmin() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return currentUser != null && currentUser.getRole() == User.UserRole.ADMIN;
    }

    public boolean isInstructor() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return currentUser != null && currentUser.getRole() == User.UserRole.INSTRUCTOR;
    }

    public boolean isStudent() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        return currentUser != null && currentUser.getRole() == User.UserRole.STUDENT;
    }

    public String getMaintenanceModeMessage() {
        return "System is in maintenance mode. Modifications are not allowed at this time.";
    }

    public String getPermissionDeniedMessage() {
        return "You do not have permission to perform this action.";
    }

    public String getAddDropClosedMessage() {
        return "The add/drop period has ended. You cannot register for or drop courses at this time.";
    }
}
