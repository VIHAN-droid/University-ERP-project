package edu.univ.erp.auth;

import edu.univ.erp.domain.User;

public class LoginResult {
    private final User user;
    private final String errorMessage;
    private final boolean accountLocked;

    private LoginResult(User user, String errorMessage, boolean accountLocked) {
        this.user = user;
        this.errorMessage = errorMessage;
        this.accountLocked = accountLocked;
    }

    public static LoginResult success(User user) {
        return new LoginResult(user, null, false);
    }

    public static LoginResult failure(String errorMessage) {
        return new LoginResult(null, errorMessage, false);
    }

    public static LoginResult locked(String errorMessage) {
        return new LoginResult(null, errorMessage, true);
    }

    public boolean isSuccess() {
        return user != null;
    }

    public User getUser() {
        return user;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }
}
