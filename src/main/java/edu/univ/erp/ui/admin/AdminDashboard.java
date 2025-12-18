package edu.univ.erp.ui.admin;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.ui.common.LoginFrame;
import edu.univ.erp.ui.common.UITheme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboard extends JFrame {
    private final AdminService adminService;
    private final SessionManager sessionManager;

    private JTable usersTable;
    private DefaultTableModel usersModel;

    private JTable coursesTable;
    private DefaultTableModel coursesModel;

    private JTable sectionsTable;
    private DefaultTableModel sectionsModel;

    private JComboBox<ComboItem> sectionCourseBox;

    private JComboBox<String> dayComboBox;

    private JLabel maintenanceBanner;

    public AdminDashboard() {
        this.adminService = new AdminService();
        this.sessionManager = SessionManager.getInstance();

        if (!sessionManager.isAdmin()) {
            dispose();
            return;
        }

        setTitle("University ERP - Admin Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        JPanel bannerPanel = new JPanel();
        bannerPanel.setLayout(new BoxLayout(bannerPanel, BoxLayout.Y_AXIS));
        bannerPanel.setBackground(UITheme.BACKGROUND);

        maintenanceBanner = UITheme.createMaintenanceBanner();
        maintenanceBanner.setVisible(adminService.isMaintenanceModeEnabled());
        bannerPanel.add(maintenanceBanner);

        JPanel topPanel = createTopPanel();

        JPanel topArea = new JPanel(new BorderLayout());
        topArea.setBackground(UITheme.BACKGROUND);
        topArea.add(bannerPanel, BorderLayout.NORTH);
        topArea.add(topPanel, BorderLayout.CENTER);

        JTabbedPane tabbedPane = new JTabbedPane();
        UITheme.styleTabbedPane(tabbedPane);

        tabbedPane.addTab("Manage Users", createManageUsersPanel());
        tabbedPane.addTab("Create User", createAddUserPanel());

        tabbedPane.addTab("Manage Courses", createManageCoursesPanel());
        tabbedPane.addTab("Create Course", createAddCoursePanel());

        tabbedPane.addTab("Manage Sections", createManageSectionsPanel());
        tabbedPane.addTab("Create Section", createAddSectionPanel());

        tabbedPane.addTab("System Settings", createSettingsPanel());

        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 5) {
                refreshSectionCourseDropdown();
            }
        });

        add(topArea, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.PRIMARY);
        topPanel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_LG, UITheme.SPACING_MD, UITheme.SPACING_LG));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(UITheme.FONT_SUBHEADER);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel userLabel = new JLabel("  •  " + sessionManager.getCurrentUser().getUsername());
        userLabel.setFont(UITheme.FONT_BODY);
        userLabel.setForeground(UITheme.TEXT_SECONDARY);

        leftPanel.add(titleLabel);
        leftPanel.add(userLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACING_SM, 0));
        buttonPanel.setOpaque(false);

        JButton changePasswordButton = new JButton("Change Password");
        UITheme.styleSecondaryButton(changePasswordButton);
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());

        JButton logoutButton = new JButton("Logout");
        UITheme.stylePrimaryButton(logoutButton);
        logoutButton.addActionListener(e -> {
            sessionManager.logout();
            new LoginFrame().setVisible(true);
            dispose();
        });

        buttonPanel.add(changePasswordButton);
        buttonPanel.add(logoutButton);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createManageUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(UITheme.SPACING_MD, UITheme.SPACING_MD));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD));

        String[] columns = {"ID", "Username", "Role", "Status", "Last Login"};
        usersModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        usersTable = new JTable(usersModel);
        UITheme.styleTable(usersTable);

        JScrollPane scrollPane = new JScrollPane(usersTable);
        UITheme.styleScrollPane(scrollPane);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACING_SM, 0));
        btnPanel.setOpaque(false);

        JButton refreshBtn = new JButton("↻ Refresh");
        UITheme.styleSecondaryButton(refreshBtn);

        JButton editBtn = new JButton("Edit Status / Reset Password");
        UITheme.stylePrimaryButton(editBtn);

        refreshBtn.addActionListener(e -> refreshUsersTable());
        editBtn.addActionListener(e -> openEditUserDialog());

        btnPanel.add(refreshBtn);
        btnPanel.add(editBtn);

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshUsersTable();
        return panel;
    }

    private void refreshUsersTable() {
        usersModel.setRowCount(0);
        List<User> users = adminService.getAllUsers();
        for (User u : users) {
            usersModel.addRow(new Object[]{
                u.getUserId(), u.getUsername(), u.getRole(), u.getStatus(), u.getLastLogin()
            });
        }
    }

    private void openEditUserDialog() {
        int row = usersTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user.");
            return;
        }

        int userId = (int) usersModel.getValueAt(row, 0);
        String username = (String) usersModel.getValueAt(row, 1);
        User.UserStatus currentStatus = (User.UserStatus) usersModel.getValueAt(row, 3);

        JDialog dialog = new JDialog(this, "Edit User: " + username, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.SURFACE);
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new GridLayout(4, 2, UITheme.SPACING_MD, UITheme.SPACING_MD));
        contentPanel.setBackground(UITheme.SURFACE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_LG, UITheme.SPACING_LG, UITheme.SPACING_LG, UITheme.SPACING_LG));

        JComboBox<User.UserStatus> statusBox = new JComboBox<>(User.UserStatus.values());
        statusBox.setSelectedItem(currentStatus);
        UITheme.styleComboBox(statusBox);

        JButton saveStatusBtn = new JButton("Update Status");
        UITheme.stylePrimaryButton(saveStatusBtn);
        saveStatusBtn.addActionListener(e -> {
            String res = adminService.updateUserStatus(userId, (User.UserStatus) statusBox.getSelectedItem());
            if (res == null) {
                JOptionPane.showMessageDialog(dialog, "Status updated.");
                refreshUsersTable();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, res);
            }
        });

        JButton resetPwdBtn = new JButton("Reset Password (to 'Supersid05')");
        UITheme.styleSecondaryButton(resetPwdBtn);
        resetPwdBtn.addActionListener(e -> {
            boolean ok = AuthService.getInstance().adminResetPassword(userId, "Supersid05");
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Password reset successfully to 'Supersid05'.");
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to reset password. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JLabel statusLabel = new JLabel("   Status:");
        statusLabel.setForeground(UITheme.TEXT_PRIMARY);

        contentPanel.add(statusLabel);
        contentPanel.add(statusBox);
        contentPanel.add(new JLabel(""));
        contentPanel.add(saveStatusBtn);
        contentPanel.add(new JLabel(""));
        contentPanel.add(resetPwdBtn);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private JPanel createAddUserPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UITheme.SPACING_SM, UITheme.SPACING_SM, UITheme.SPACING_SM, UITheme.SPACING_SM);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userField = new JTextField(15);
        UITheme.styleTextField(userField);

        JPasswordField passField = new JPasswordField(15);
        UITheme.stylePasswordField(passField);

        JComboBox<User.UserRole> roleCombo = new JComboBox<>(User.UserRole.values());
        UITheme.styleComboBox(roleCombo);

        JTextField info1Field = new JTextField(15);
        UITheme.styleTextField(info1Field);

        JTextField info2Field = new JTextField(15);
        UITheme.styleTextField(info2Field);

        JLabel l1 = new JLabel("Roll No / Emp ID:");
        l1.setForeground(UITheme.TEXT_PRIMARY);
        JLabel l2 = new JLabel("Year / Dept:");
        l2.setForeground(UITheme.TEXT_PRIMARY);

        JButton createBtn = new JButton("Create User");
        UITheme.stylePrimaryButton(createBtn);
        createBtn.addActionListener(e -> {
            String res = adminService.createUser(
                userField.getText(),
                new String(passField.getPassword()),
                (User.UserRole) roleCombo.getSelectedItem(),
                info1Field.getText(),
                info2Field.getText()
            );
            if (res == null) {
                JOptionPane.showMessageDialog(this, "User Created!");
                userField.setText(""); passField.setText("");
                info1Field.setText(""); info2Field.setText("");
                refreshUsersTable();
            } else {
                JOptionPane.showMessageDialog(this, res);
            }
        });

        int y=0;
        gbc.gridx=0; gbc.gridy=y;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(usernameLabel, gbc);
        gbc.gridx=1; panel.add(userField, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(passwordLabel, gbc);
        gbc.gridx=1; panel.add(passField, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(roleLabel, gbc);
        gbc.gridx=1; panel.add(roleCombo, gbc);

        y++; gbc.gridx=0; gbc.gridy=y; panel.add(l1, gbc);
        gbc.gridx=1; panel.add(info1Field, gbc);

        y++; gbc.gridx=0; gbc.gridy=y; panel.add(l2, gbc);
        gbc.gridx=1; panel.add(info2Field, gbc);

        y++; gbc.gridx=1; gbc.gridy=y; panel.add(createBtn, gbc);

        return panel;
    }

    private JPanel createManageCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(UITheme.SPACING_MD, UITheme.SPACING_MD));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD));

        String[] columns = {"ID", "Code", "Title", "Credits", "Description"};
        coursesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        coursesTable = new JTable(coursesModel);
        UITheme.styleTable(coursesTable);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        UITheme.styleScrollPane(scrollPane);

        JButton refreshBtn = new JButton("↻ Refresh");
        UITheme.styleSecondaryButton(refreshBtn);

        JButton editBtn = new JButton("Edit Course");
        UITheme.stylePrimaryButton(editBtn);

        refreshBtn.addActionListener(e -> refreshCoursesTable());
        editBtn.addActionListener(e -> openEditCourseDialog());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACING_SM, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(refreshBtn);
        btnPanel.add(editBtn);

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshCoursesTable();
        return panel;
    }

    private void refreshCoursesTable() {
        coursesModel.setRowCount(0);
        List<Course> courses = adminService.getAllCourses();
        for (Course c : courses) {
            coursesModel.addRow(new Object[]{c.getCourseId(), c.getCode(), c.getTitle(), c.getCredits(), c.getDescription()});
        }
    }

    private void openEditCourseDialog() {
        int row = coursesTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a course."); return; }

        int id = (int) coursesModel.getValueAt(row, 0);
        String code = (String) coursesModel.getValueAt(row, 1);
        String title = (String) coursesModel.getValueAt(row, 2);
        int credits = (int) coursesModel.getValueAt(row, 3);
        String desc = (String) coursesModel.getValueAt(row, 4);

        JDialog d = new JDialog(this, "Edit Course", true);
        d.getContentPane().setBackground(UITheme.SURFACE);
        d.setLayout(new BorderLayout());
        d.setSize(450, 350);
        d.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new GridLayout(5, 2, UITheme.SPACING_MD, UITheme.SPACING_MD));
        contentPanel.setBackground(UITheme.SURFACE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_LG, UITheme.SPACING_LG, UITheme.SPACING_LG, UITheme.SPACING_LG));

        JTextField cField = new JTextField(code);
        UITheme.styleTextField(cField);

        JTextField tField = new JTextField(title);
        UITheme.styleTextField(tField);

        JTextField crField = new JTextField(String.valueOf(credits));
        UITheme.styleTextField(crField);

        JTextField dField = new JTextField(desc);
        UITheme.styleTextField(dField);

        JButton save = new JButton("Save");
        UITheme.stylePrimaryButton(save);

        save.addActionListener(e -> {
            Course c = new Course(cField.getText(), tField.getText(), Integer.parseInt(crField.getText()));
            c.setCourseId(id);
            c.setDescription(dField.getText());

            String res = adminService.updateCourse(c);
            if (res == null) {
                JOptionPane.showMessageDialog(d, "Course Updated.");
                refreshCoursesTable();
                d.dispose();
            } else {
                JOptionPane.showMessageDialog(d, res);
            }
        });

        JLabel codeLabel = new JLabel("Code:");
        codeLabel.setForeground(UITheme.TEXT_PRIMARY);
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        JLabel creditsLabel = new JLabel("Credits:");
        creditsLabel.setForeground(UITheme.TEXT_PRIMARY);
        JLabel descLabel = new JLabel("Desc:");
        descLabel.setForeground(UITheme.TEXT_PRIMARY);

        contentPanel.add(codeLabel); contentPanel.add(cField);
        contentPanel.add(titleLabel); contentPanel.add(tField);
        contentPanel.add(creditsLabel); contentPanel.add(crField);
        contentPanel.add(descLabel); contentPanel.add(dField);
        contentPanel.add(new JLabel("")); contentPanel.add(save);

        d.add(contentPanel, BorderLayout.CENTER);
        d.setVisible(true);
    }

    private JPanel createAddCoursePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UITheme.SPACING_SM, UITheme.SPACING_SM, UITheme.SPACING_SM, UITheme.SPACING_SM);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField codeField = new JTextField(15);
        UITheme.styleTextField(codeField);

        JTextField titleField = new JTextField(15);
        UITheme.styleTextField(titleField);

        JTextField credField = new JTextField(15);
        UITheme.styleTextField(credField);

        JTextField descField = new JTextField(15);
        UITheme.styleTextField(descField);

        JButton addBtn = new JButton("Add Course");
        UITheme.stylePrimaryButton(addBtn);

        addBtn.addActionListener(e -> {
            try {
                String res = adminService.createCourse(
                    codeField.getText(), titleField.getText(),
                    Integer.parseInt(credField.getText()), descField.getText()
                );
                if (res == null) {
                    JOptionPane.showMessageDialog(this, "Course Added!");
                    codeField.setText(""); titleField.setText("");
                    credField.setText(""); descField.setText("");
                    refreshCoursesTable();
                } else {
                    JOptionPane.showMessageDialog(this, res);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid credits");
            }
        });

        int y = 0;
        gbc.gridx=0; gbc.gridy=y;
        JLabel codeLabel = new JLabel("Code (CS101):");
        codeLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(codeLabel, gbc);
        gbc.gridx=1; panel.add(codeField, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(titleLabel, gbc);
        gbc.gridx=1; panel.add(titleField, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel creditsLabel = new JLabel("Credits:");
        creditsLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(creditsLabel, gbc);
        gbc.gridx=1; panel.add(credField, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel descLabel = new JLabel("Description:");
        descLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(descLabel, gbc);
        gbc.gridx=1; panel.add(descField, gbc);

        y++; gbc.gridx=1; gbc.gridy=y; panel.add(addBtn, gbc);

        return panel;
    }

    private JPanel createManageSectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(UITheme.SPACING_MD, UITheme.SPACING_MD));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD));

        String[] columns = {"ID", "Course", "Sec Code", "Instructor", "Room", "Time", "Cap", "Enrolled", "Status"};
        sectionsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        sectionsTable = new JTable(sectionsModel);
        UITheme.styleTable(sectionsTable);

        JScrollPane scrollPane = new JScrollPane(sectionsTable);
        UITheme.styleScrollPane(scrollPane);

        JButton refreshBtn = new JButton("↻ Refresh");
        UITheme.styleSecondaryButton(refreshBtn);

        JButton editBtn = new JButton("Edit / Assign Instructor");
        UITheme.stylePrimaryButton(editBtn);

        JButton deleteBtn = new JButton("Delete Section");
        deleteBtn.setBackground(UITheme.ERROR);
        deleteBtn.setForeground(UITheme.TEXT_PRIMARY);
        deleteBtn.setFont(UITheme.FONT_BODY);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        refreshBtn.addActionListener(e -> refreshSectionsTable());
        editBtn.addActionListener(e -> openEditSectionDialog());
        deleteBtn.addActionListener(e -> deleteSection());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACING_SM, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(refreshBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshSectionsTable();
        return panel;
    }

    private void deleteSection() {
        int row = sectionsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to delete.");
            return;
        }

        int sectionId = (int) sectionsModel.getValueAt(row, 0);
        int enrolled = (int) sectionsModel.getValueAt(row, 7);

        if (enrolled > 0) {
            JOptionPane.showMessageDialog(this,
                "Cannot delete section with " + enrolled + " enrolled students.\n" +
                "Please remove all enrollments first or mark the section as completed.",
                "Delete Blocked", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this section?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String result = adminService.deleteSection(sectionId);
            if (result == null) {
                JOptionPane.showMessageDialog(this, "Section deleted successfully.");
                refreshSectionsTable();
            } else {
                JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshSectionsTable() {
        sectionsModel.setRowCount(0);
        List<Section> sections = adminService.getAllSections();
        for (Section s : sections) {
            sectionsModel.addRow(new Object[]{
                s.getSectionId(), s.getCourseCode(), s.getSectionCode(),
                s.getInstructorName(), s.getRoom(), s.getDayTime(),
                s.getCapacity(), s.getEnrolledCount(), s.getStatus()
            });
        }
    }

    private void openEditSectionDialog() {
        int row = sectionsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a section."); return; }

        int secId = (int) sectionsModel.getValueAt(row, 0);

        JDialog d = new JDialog(this, "Edit Section", true);
        d.getContentPane().setBackground(UITheme.SURFACE);
        d.setSize(600, 550);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(UITheme.SURFACE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_LG, UITheme.SPACING_LG, UITheme.SPACING_LG, UITheme.SPACING_LG));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UITheme.SPACING_SM, UITheme.SPACING_SM, UITheme.SPACING_SM, UITheme.SPACING_SM);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField roomF = new JTextField((String) sectionsModel.getValueAt(row, 4));
        UITheme.styleTextField(roomF);

        String currentDayTime = (String) sectionsModel.getValueAt(row, 5);

        JComboBox<String> editDayCombo = new JComboBox<>(UITheme.DAYS);
        UITheme.styleComboBox(editDayCombo);

        JTextField editStartTime = new JTextField("09:00", 6);
        UITheme.styleTextField(editStartTime);

        JTextField editEndTime = new JTextField("10:30", 6);
        UITheme.styleTextField(editEndTime);

        DefaultListModel<String> editDayTimeListModel = new DefaultListModel<>();
        JList<String> editDayTimeList = new JList<>(editDayTimeListModel);
        editDayTimeList.setFont(UITheme.FONT_BODY);
        editDayTimeList.setBackground(UITheme.SURFACE_DARK);
        editDayTimeList.setForeground(UITheme.TEXT_PRIMARY);
        editDayTimeList.setSelectionBackground(UITheme.TABLE_SELECTED);
        editDayTimeList.setVisibleRowCount(3);
        JScrollPane editListScrollPane = new JScrollPane(editDayTimeList);
        editListScrollPane.setPreferredSize(new Dimension(250, 70));
        UITheme.styleScrollPane(editListScrollPane);

        if (currentDayTime != null && !currentDayTime.isEmpty()) {
            java.util.List<String> entries = UITheme.parseMultipleDayTimes(currentDayTime);
            for (String entry : entries) {
                editDayTimeListModel.addElement(entry);
            }
        }

        JButton editAddTimeBtn = new JButton("+");
        UITheme.styleSecondaryButton(editAddTimeBtn);
        editAddTimeBtn.addActionListener(e -> {
            String day = (String) editDayCombo.getSelectedItem();
            String startTime = editStartTime.getText().trim();
            String endTime = editEndTime.getText().trim();

            if (!UITheme.isValidTime(startTime)) {
                JOptionPane.showMessageDialog(d, "Invalid start time. Use HH:MM (24-hour format).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!UITheme.isValidTime(endTime)) {
                JOptionPane.showMessageDialog(d, "Invalid end time. Use HH:MM (24-hour format).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String timeRange = startTime + "-" + endTime;
            if (!UITheme.isValidTimeRange(timeRange)) {
                JOptionPane.showMessageDialog(d, "End time must be after start time.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String entry = UITheme.fullToShortDayName(day) + " " + timeRange;
            for (int i = 0; i < editDayTimeListModel.size(); i++) {
                if (editDayTimeListModel.get(i).equals(entry)) {
                    JOptionPane.showMessageDialog(d, "This time slot already exists.", "Duplicate Entry", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            editDayTimeListModel.addElement(entry);
        });

        JButton editRemoveTimeBtn = new JButton("-");
        UITheme.styleSecondaryButton(editRemoveTimeBtn);
        editRemoveTimeBtn.addActionListener(e -> {
            int selectedIndex = editDayTimeList.getSelectedIndex();
            if (selectedIndex >= 0) {
                editDayTimeListModel.remove(selectedIndex);
            }
        });

        JPanel editTimeEntryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACING_XS, 0));
        editTimeEntryPanel.setOpaque(false);
        editTimeEntryPanel.add(editDayCombo);
        JLabel fromLabel = new JLabel("from");
        fromLabel.setForeground(UITheme.TEXT_SECONDARY);
        editTimeEntryPanel.add(fromLabel);
        editTimeEntryPanel.add(editStartTime);
        JLabel toLabel = new JLabel("to");
        toLabel.setForeground(UITheme.TEXT_SECONDARY);
        editTimeEntryPanel.add(toLabel);
        editTimeEntryPanel.add(editEndTime);
        editTimeEntryPanel.add(editAddTimeBtn);
        editTimeEntryPanel.add(editRemoveTimeBtn);

        JTextField capF = new JTextField(String.valueOf(sectionsModel.getValueAt(row, 6)));
        UITheme.styleTextField(capF);

        JComboBox<Section.SectionStatus> statBox = new JComboBox<>(Section.SectionStatus.values());
        UITheme.styleComboBox(statBox);
        statBox.setSelectedItem(sectionsModel.getValueAt(row, 8));

        List<Instructor> insts = adminService.getAllInstructors();
        JComboBox<ComboItem> instBox = new JComboBox<>();
        UITheme.styleComboBox(instBox);
        instBox.addItem(new ComboItem("None", 0));

        String currentInstName = (String) sectionsModel.getValueAt(row, 3);

        for (Instructor i : insts) {
            ComboItem item = new ComboItem(i.getEmployeeId() + " (ID:" + i.getInstructorId() + ")", i.getInstructorId());
            instBox.addItem(item);
        }

        JButton save = new JButton("Save Changes");
        UITheme.stylePrimaryButton(save);
        save.addActionListener(e -> {
            Section s = new Section();
            s.setSectionId(secId);
            s.setSectionCode((String) sectionsModel.getValueAt(row, 2));
            s.setRoom(roomF.getText());

            if (editDayTimeListModel.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Please add at least one time slot.");
                return;
            }

            java.util.List<String> entries = new java.util.ArrayList<>();
            for (int i = 0; i < editDayTimeListModel.size(); i++) {
                entries.add(editDayTimeListModel.get(i));
            }
            s.setDayTime(UITheme.formatMultipleDayTimes(entries));

            try {
                s.setCapacity(Integer.parseInt(capF.getText()));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(d, "Invalid capacity. Please enter a number.");
                return;
            }
            s.setStatus((Section.SectionStatus) statBox.getSelectedItem());
            s.setSemester("Fall");
            s.setYear(2024);

            ComboItem selInst = (ComboItem) instBox.getSelectedItem();
            if (selInst != null && selInst.id != 0) s.setInstructorId(selInst.id);

            String res = adminService.updateSection(s);
            if (res == null) {
                JOptionPane.showMessageDialog(d, "Section Updated");
                refreshSectionsTable();
                d.dispose();
            } else {
                JOptionPane.showMessageDialog(d, res);
            }
        });

        int y = 0;

        gbc.gridx=0; gbc.gridy=y;
        JLabel roomLabel = new JLabel("Room:");
        roomLabel.setForeground(UITheme.TEXT_PRIMARY);
        contentPanel.add(roomLabel, gbc);
        gbc.gridx=1; contentPanel.add(roomF, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel timeEntryLabel = new JLabel("Add Time Slot:");
        timeEntryLabel.setForeground(UITheme.TEXT_PRIMARY);
        contentPanel.add(timeEntryLabel, gbc);
        gbc.gridx=1; contentPanel.add(editTimeEntryPanel, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel timeSlotsLabel = new JLabel("Time Slots:");
        timeSlotsLabel.setForeground(UITheme.TEXT_PRIMARY);
        contentPanel.add(timeSlotsLabel, gbc);
        gbc.gridx=1; contentPanel.add(editListScrollPane, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel capLabel = new JLabel("Capacity:");
        capLabel.setForeground(UITheme.TEXT_PRIMARY);
        contentPanel.add(capLabel, gbc);
        gbc.gridx=1; contentPanel.add(capF, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel instLabel = new JLabel("Assign Instructor:");
        instLabel.setForeground(UITheme.TEXT_PRIMARY);
        contentPanel.add(instLabel, gbc);
        gbc.gridx=1; contentPanel.add(instBox, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel statLabel = new JLabel("Status:");
        statLabel.setForeground(UITheme.TEXT_PRIMARY);
        contentPanel.add(statLabel, gbc);
        gbc.gridx=1; contentPanel.add(statBox, gbc);

        y++; gbc.gridx=1; gbc.gridy=y;
        contentPanel.add(save, gbc);

        d.add(contentPanel, BorderLayout.CENTER);
        d.setVisible(true);
    }

    private JPanel createAddSectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UITheme.SPACING_SM, UITheme.SPACING_SM, UITheme.SPACING_SM, UITheme.SPACING_SM);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        sectionCourseBox = new JComboBox<>();
        UITheme.styleComboBox(sectionCourseBox);
        refreshSectionCourseDropdown();

        JTextField codeF = new JTextField(15);
        UITheme.styleTextField(codeF);

        JTextField roomF = new JTextField(15);
        UITheme.styleTextField(roomF);

        dayComboBox = new JComboBox<>(UITheme.DAYS);
        UITheme.styleComboBox(dayComboBox);

        JTextField startTimeField = new JTextField("09:00", 6);
        UITheme.styleTextField(startTimeField);

        JTextField endTimeField = new JTextField("10:30", 6);
        UITheme.styleTextField(endTimeField);

        DefaultListModel<String> dayTimeListModel = new DefaultListModel<>();
        JList<String> dayTimeList = new JList<>(dayTimeListModel);
        dayTimeList.setFont(UITheme.FONT_BODY);
        dayTimeList.setBackground(UITheme.SURFACE_DARK);
        dayTimeList.setForeground(UITheme.TEXT_PRIMARY);
        dayTimeList.setSelectionBackground(UITheme.TABLE_SELECTED);
        dayTimeList.setVisibleRowCount(3);
        JScrollPane listScrollPane = new JScrollPane(dayTimeList);
        listScrollPane.setPreferredSize(new Dimension(250, 70));
        UITheme.styleScrollPane(listScrollPane);

        JButton addTimeBtn = new JButton("+ Add Time Slot");
        UITheme.styleSecondaryButton(addTimeBtn);
        addTimeBtn.addActionListener(e -> {
            String day = (String) dayComboBox.getSelectedItem();
            String startTime = startTimeField.getText().trim();
            String endTime = endTimeField.getText().trim();

            if (!UITheme.isValidTime(startTime)) {
                JOptionPane.showMessageDialog(this, "Invalid start time format. Use HH:MM (24-hour format).\nExample: 09:00, 14:30", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!UITheme.isValidTime(endTime)) {
                JOptionPane.showMessageDialog(this, "Invalid end time format. Use HH:MM (24-hour format).\nExample: 10:30, 16:00", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String timeRange = startTime + "-" + endTime;
            if (!UITheme.isValidTimeRange(timeRange)) {
                JOptionPane.showMessageDialog(this, "End time must be after start time.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String entry = UITheme.fullToShortDayName(day) + " " + timeRange;

            for (int i = 0; i < dayTimeListModel.size(); i++) {
                if (dayTimeListModel.get(i).equals(entry)) {
                    JOptionPane.showMessageDialog(this, "This time slot already exists.", "Duplicate Entry", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            dayTimeListModel.addElement(entry);
        });

        JButton removeTimeBtn = new JButton("- Remove");
        UITheme.styleSecondaryButton(removeTimeBtn);
        removeTimeBtn.addActionListener(e -> {
            int selectedIndex = dayTimeList.getSelectedIndex();
            if (selectedIndex >= 0) {
                dayTimeListModel.remove(selectedIndex);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a time slot to remove.");
            }
        });

        JPanel timeEntryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACING_XS, 0));
        timeEntryPanel.setOpaque(false);
        timeEntryPanel.add(dayComboBox);
        JLabel fromLabel = new JLabel("from");
        fromLabel.setForeground(UITheme.TEXT_SECONDARY);
        timeEntryPanel.add(fromLabel);
        timeEntryPanel.add(startTimeField);
        JLabel toLabel = new JLabel("to");
        toLabel.setForeground(UITheme.TEXT_SECONDARY);
        timeEntryPanel.add(toLabel);
        timeEntryPanel.add(endTimeField);
        timeEntryPanel.add(addTimeBtn);

        JPanel timeListPanel = new JPanel(new BorderLayout(UITheme.SPACING_SM, 0));
        timeListPanel.setOpaque(false);
        timeListPanel.add(listScrollPane, BorderLayout.CENTER);
        timeListPanel.add(removeTimeBtn, BorderLayout.EAST);

        JTextField capF = new JTextField(15);
        UITheme.styleTextField(capF);

        JTextField semF = new JTextField("Fall", 15);
        UITheme.styleTextField(semF);

        JTextField yearF = new JTextField("2025", 15);
        UITheme.styleTextField(yearF);

        JButton add = new JButton("Create Section");
        UITheme.stylePrimaryButton(add);
        add.addActionListener(e -> {
            ComboItem ci = (ComboItem) sectionCourseBox.getSelectedItem();
            if(ci == null) {
                JOptionPane.showMessageDialog(this, "Please select a course first.");
                return;
            }

            try {
                int capacity = Integer.parseInt(capF.getText());
                if (capacity <= 0) {
                    JOptionPane.showMessageDialog(this, "Capacity must be a positive number.");
                    return;
                }

                if (dayTimeListModel.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please add at least one time slot.");
                    return;
                }

                java.util.List<String> entries = new java.util.ArrayList<>();
                for (int i = 0; i < dayTimeListModel.size(); i++) {
                    entries.add(dayTimeListModel.get(i));
                }
                String dayTime = UITheme.formatMultipleDayTimes(entries);

                String res = adminService.createSection(
                    ci.id, null, codeF.getText(), dayTime, roomF.getText(),
                    capacity, semF.getText(), Integer.parseInt(yearF.getText())
                );
                if(res == null) {
                    JOptionPane.showMessageDialog(this, "Section Created");
                    refreshSectionsTable();

                    codeF.setText("");
                    roomF.setText("");
                    capF.setText("");
                    dayTimeListModel.clear();
                    startTimeField.setText("09:00");
                    endTimeField.setText("10:30");
                    dayComboBox.setSelectedIndex(0);
                } else {
                    JOptionPane.showMessageDialog(this, res);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for capacity and year.");
            }
        });

        int y = 0;
        gbc.gridx=0; gbc.gridy=y;
        JLabel courseLabel = new JLabel("Course:");
        courseLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(courseLabel, gbc);
        gbc.gridx=1; panel.add(sectionCourseBox, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel secCodeLabel = new JLabel("Sec Code (A/B):");
        secCodeLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(secCodeLabel, gbc);
        gbc.gridx=1; panel.add(codeF, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel roomLabel = new JLabel("Room:");
        roomLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(roomLabel, gbc);
        gbc.gridx=1; panel.add(roomF, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel timeLabel = new JLabel("Add Time Slot:");
        timeLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(timeLabel, gbc);
        gbc.gridx=1; panel.add(timeEntryPanel, gbc);

        y++; gbc.gridx=1; gbc.gridy=y;
        JLabel noteLabel = new JLabel("<html><i style='color:#B0BEC5;font-size:10px'>Enter times in 24-hour format (e.g., 09:00 to 10:30). Add multiple slots for different days.</i></html>");
        panel.add(noteLabel, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel slotsLabel = new JLabel("Time Slots:");
        slotsLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(slotsLabel, gbc);
        gbc.gridx=1; panel.add(timeListPanel, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel capLabel = new JLabel("Capacity:");
        capLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(capLabel, gbc);
        gbc.gridx=1; panel.add(capF, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel semLabel = new JLabel("Semester:");
        semLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(semLabel, gbc);
        gbc.gridx=1; panel.add(semF, gbc);

        y++; gbc.gridx=0; gbc.gridy=y;
        JLabel yearLabel = new JLabel("Year:");
        yearLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(yearLabel, gbc);
        gbc.gridx=1; panel.add(yearF, gbc);

        y++; gbc.gridx=1; gbc.gridy=y; panel.add(add, gbc);

        return panel;
    }

    private void refreshSectionCourseDropdown() {
        if (sectionCourseBox != null) {
            sectionCourseBox.removeAllItems();
            List<Course> courses = adminService.getAllCourses();
            for (Course c : courses) {
                sectionCourseBox.addItem(new ComboItem(c.getCode() + " - " + c.getTitle(), c.getCourseId()));
            }
        }
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_LG, UITheme.SPACING_LG, UITheme.SPACING_LG, UITheme.SPACING_LG));

        JPanel maintenancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        maintenancePanel.setBackground(UITheme.SURFACE);
        maintenancePanel.setBorder(UITheme.createTitledBorder("Maintenance Mode"));

        JLabel maintenanceStatusLabel = new JLabel("Current Status: " +
            (adminService.isMaintenanceModeEnabled() ? "ON (System Locked)" : "OFF (Normal)"));
        maintenanceStatusLabel.setForeground(UITheme.TEXT_PRIMARY);

        JToggleButton maintenanceToggleBtn = new JToggleButton("Toggle Maintenance Mode");
        UITheme.stylePrimaryButton(maintenanceToggleBtn);
        maintenanceToggleBtn.setSelected(adminService.isMaintenanceModeEnabled());
        maintenanceToggleBtn.addActionListener(e -> {
            boolean current = adminService.isMaintenanceModeEnabled();
            adminService.toggleMaintenanceMode(!current);
            maintenanceStatusLabel.setText("Current Status: " +
                (!current ? "ON (System Locked)" : "OFF (Normal)"));
            maintenanceToggleBtn.setSelected(!current);
            maintenanceBanner.setVisible(!current);
        });

        maintenancePanel.add(maintenanceStatusLabel);
        maintenancePanel.add(Box.createHorizontalStrut(UITheme.SPACING_LG));
        maintenancePanel.add(maintenanceToggleBtn);

        JPanel addDropPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addDropPanel.setBackground(UITheme.SURFACE);
        addDropPanel.setBorder(UITheme.createTitledBorder("Course Add/Drop Period"));

        JLabel addDropStatusLabel = new JLabel("Add/Drop Status: " +
            (adminService.isAddDropEnabled() ? "OPEN (Students can register/drop)" : "CLOSED (Registration locked)"));
        addDropStatusLabel.setForeground(UITheme.TEXT_PRIMARY);

        JToggleButton addDropToggleBtn = new JToggleButton("Toggle Add/Drop Period");
        UITheme.stylePrimaryButton(addDropToggleBtn);
        addDropToggleBtn.setSelected(adminService.isAddDropEnabled());
        addDropToggleBtn.addActionListener(e -> {
            boolean current = adminService.isAddDropEnabled();
            String result = adminService.toggleAddDropPeriod(!current);
            if (result == null) {
                addDropStatusLabel.setText("Add/Drop Status: " +
                    (!current ? "OPEN (Students can register/drop)" : "CLOSED (Registration locked)"));
                addDropToggleBtn.setSelected(!current);
                JOptionPane.showMessageDialog(this,
                    !current ? "Add/Drop period is now OPEN. Students can register and drop courses."
                             : "Add/Drop period is now CLOSED. Students cannot register or drop courses.",
                    "Add/Drop Period Updated", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, result, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JLabel addDropNote = new JLabel("<html><i>Note: When closed, students will see a message that the add/drop period has ended.</i></html>");
        addDropNote.setFont(UITheme.FONT_SMALL);
        addDropNote.setForeground(UITheme.TEXT_SECONDARY);

        addDropPanel.add(addDropStatusLabel);
        addDropPanel.add(Box.createHorizontalStrut(UITheme.SPACING_LG));
        addDropPanel.add(addDropToggleBtn);
        addDropPanel.add(Box.createHorizontalStrut(UITheme.SPACING_SM));
        addDropPanel.add(addDropNote);

        JPanel backupPanel = new JPanel();
        backupPanel.setLayout(new BoxLayout(backupPanel, BoxLayout.Y_AXIS));
        backupPanel.setBackground(UITheme.SURFACE);
        backupPanel.setBorder(UITheme.createTitledBorder("Database Backup & Restore"));

        edu.univ.erp.service.BackupService backupService = new edu.univ.erp.service.BackupService();
        JLabel backupDirLabel = new JLabel("Backup Directory: " + backupService.getBackupDirectory());
        backupDirLabel.setFont(UITheme.FONT_SMALL);
        backupDirLabel.setForeground(UITheme.TEXT_SECONDARY);
        backupDirLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        DefaultListModel<edu.univ.erp.service.BackupService.BackupInfo> backupListModel = new DefaultListModel<>();
        JList<edu.univ.erp.service.BackupService.BackupInfo> backupList = new JList<>(backupListModel);
        backupList.setFont(UITheme.FONT_BODY);
        backupList.setBackground(UITheme.SURFACE_DARK);
        backupList.setForeground(UITheme.TEXT_PRIMARY);
        backupList.setSelectionBackground(UITheme.TABLE_SELECTED);
        backupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane backupScrollPane = new JScrollPane(backupList);
        backupScrollPane.setPreferredSize(new Dimension(500, 120));
        backupScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        UITheme.styleScrollPane(backupScrollPane);

        Runnable refreshBackupList = () -> {
            backupListModel.clear();
            for (edu.univ.erp.service.BackupService.BackupInfo info : backupService.getAvailableBackups()) {
                backupListModel.addElement(info);
            }
        };
        refreshBackupList.run();

        JPanel backupButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACING_SM, 0));
        backupButtonPanel.setOpaque(false);
        backupButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton createBackupBtn = new JButton("Create Backup");
        UITheme.stylePrimaryButton(createBackupBtn);
        createBackupBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Create a backup of the current database?\n" +
                "This will save all data to a file in the backups folder.",
                "Confirm Backup", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                edu.univ.erp.service.BackupService.BackupResult result = backupService.createBackup();
                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(this,
                        "Backup created successfully!\n" +
                        "File: " + result.getFilePath(),
                        "Backup Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshBackupList.run();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Backup failed: " + result.getMessage(),
                        "Backup Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton restoreBackupBtn = new JButton("Restore Selected");
        UITheme.styleSecondaryButton(restoreBackupBtn);
        restoreBackupBtn.addActionListener(e -> {
            edu.univ.erp.service.BackupService.BackupInfo selected = backupList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select a backup to restore.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                "WARNING: This will REPLACE all current data with the backup!\n\n" +
                "Selected backup: " + selected.getFileName() + "\n" +
                "Created: " + selected.getFormattedDate() + "\n\n" +
                "Are you sure you want to restore this backup?",
                "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                edu.univ.erp.service.BackupService.BackupResult result = backupService.restoreBackup(selected.getFullPath());
                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(this,
                        "Database restored successfully!\n" +
                        "Please restart the application to see all changes.",
                        "Restore Success", JOptionPane.INFORMATION_MESSAGE);

                    refreshUsersTable();
                    refreshCoursesTable();
                    refreshSectionsTable();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Restore failed: " + result.getMessage(),
                        "Restore Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton refreshListBtn = new JButton("↻ Refresh");
        UITheme.styleSecondaryButton(refreshListBtn);
        refreshListBtn.addActionListener(e -> refreshBackupList.run());

        JButton deleteBackupBtn = new JButton("Delete Selected");
        deleteBackupBtn.setBackground(UITheme.ERROR);
        deleteBackupBtn.setForeground(UITheme.TEXT_PRIMARY);
        deleteBackupBtn.setFont(UITheme.FONT_BODY);
        deleteBackupBtn.setFocusPainted(false);
        deleteBackupBtn.setBorderPainted(false);
        deleteBackupBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBackupBtn.addActionListener(e -> {
            edu.univ.erp.service.BackupService.BackupInfo selected = backupList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select a backup to delete.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete backup: " + selected.getFileName() + "?\n" +
                "This action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (backupService.deleteBackup(selected.getFullPath())) {
                    JOptionPane.showMessageDialog(this, "Backup deleted.");
                    refreshBackupList.run();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete backup.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        backupButtonPanel.add(createBackupBtn);
        backupButtonPanel.add(restoreBackupBtn);
        backupButtonPanel.add(refreshListBtn);
        backupButtonPanel.add(deleteBackupBtn);

        JLabel backupNote = new JLabel("<html><i>Note: Create backups before making major changes. Restore reverts all data to the backup state.</i></html>");
        backupNote.setFont(UITheme.FONT_SMALL);
        backupNote.setForeground(UITheme.TEXT_SECONDARY);
        backupNote.setAlignmentX(Component.LEFT_ALIGNMENT);

        backupPanel.add(backupDirLabel);
        backupPanel.add(Box.createVerticalStrut(UITheme.SPACING_SM));
        backupPanel.add(backupScrollPane);
        backupPanel.add(Box.createVerticalStrut(UITheme.SPACING_SM));
        backupPanel.add(backupButtonPanel);
        backupPanel.add(Box.createVerticalStrut(UITheme.SPACING_SM));
        backupPanel.add(backupNote);

        panel.add(maintenancePanel);
        panel.add(Box.createVerticalStrut(UITheme.SPACING_LG));
        panel.add(addDropPanel);
        panel.add(Box.createVerticalStrut(UITheme.SPACING_LG));
        panel.add(backupPanel);

        return panel;
    }

    private void showChangePasswordDialog() {
        new edu.univ.erp.ui.common.ChangePasswordDialog(this).setVisible(true);
    }

    static class ComboItem {
        String label;
        int id;
        public ComboItem(String label, int id) { this.label = label; this.id = id; }
        public String toString() { return label; }
    }
}