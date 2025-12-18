package edu.univ.erp.ui.common;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.util.MessageUtil;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    private final JPasswordField currentPasswordField;
    private final JPasswordField newPasswordField;
    private final JPasswordField confirmPasswordField;
    private final JButton changeButton;
    private final JButton cancelButton;

    public ChangePasswordDialog(Frame parent) {
        super(parent, "Change Password", true);

        currentPasswordField = new JPasswordField(20);
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        changeButton = new JButton("Change Password");
        cancelButton = new JButton("Cancel");

        UITheme.stylePasswordField(currentPasswordField);
        UITheme.stylePasswordField(newPasswordField);
        UITheme.stylePasswordField(confirmPasswordField);
        UITheme.stylePrimaryButton(changeButton);
        UITheme.styleSecondaryButton(cancelButton);

        initLayout();
        initActions();

        setSize(400, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initLayout() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UITheme.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UITheme.SPACING_SM, UITheme.SPACING_SM, UITheme.SPACING_SM, UITheme.SPACING_SM);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        JLabel titleLabel = new JLabel("Change Password");
        titleLabel.setFont(UITheme.FONT_SUBHEADER);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        JLabel currentLabel = new JLabel("Current Password:");
        currentLabel.setForeground(UITheme.TEXT_PRIMARY);
        mainPanel.add(currentLabel, gbc);
        gbc.gridx = 1;
        mainPanel.add(currentPasswordField, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        JLabel newLabel = new JLabel("New Password:");
        newLabel.setForeground(UITheme.TEXT_PRIMARY);
        mainPanel.add(newLabel, gbc);
        gbc.gridx = 1;
        mainPanel.add(newPasswordField, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setForeground(UITheme.TEXT_PRIMARY);
        mainPanel.add(confirmLabel, gbc);
        gbc.gridx = 1;
        mainPanel.add(confirmPasswordField, gbc);

        y++;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, UITheme.SPACING_SM, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(cancelButton);
        buttonPanel.add(changeButton);

        gbc.gridx = 0; gbc.gridy = y;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_LG, UITheme.SPACING_LG, UITheme.SPACING_LG, UITheme.SPACING_LG));
        setContentPane(mainPanel);
    }

    private void initActions() {
        changeButton.addActionListener(e -> performChange());
        cancelButton.addActionListener(e -> dispose());

        confirmPasswordField.addActionListener(e -> performChange());
    }

    private void performChange() {
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            MessageUtil.showError(this, "Please fill in all fields.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            MessageUtil.showError(this, "New passwords do not match.");
            confirmPasswordField.setText("");
            return;
        }

        String passwordError = validatePassword(newPassword);
        if (passwordError != null) {
            MessageUtil.showError(this, passwordError);
            return;
        }

        int userId = SessionManager.getInstance().getCurrentUser().getUserId();
        boolean success = AuthService.getInstance().changePassword(userId, currentPassword, newPassword);

        if (success) {
            MessageUtil.showSuccess(this, "Password changed successfully.");
            dispose();
        } else {
            MessageUtil.showError(this, "Failed to change password. Please check your current password.");
            currentPasswordField.setText("");
        }
    }

    private String validatePassword(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters long.";
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        if (!hasUpper || !hasLower || !hasDigit) {
            return "Password must contain at least one uppercase letter, one lowercase letter, and one digit.";
        }

        return null;
    }
}
