package edu.univ.erp.auth;

import edu.univ.erp.domain.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean hasRole(User.UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public boolean isAdmin() {
        return hasRole(User.UserRole.ADMIN);
    }

    public boolean isInstructor() {
        return hasRole(User.UserRole.INSTRUCTOR);
    }

    public boolean isStudent() {
        return hasRole(User.UserRole.STUDENT);
    }
}
