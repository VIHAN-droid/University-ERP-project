package edu.univ.erp.ui.instructor;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.ui.common.LoginFrame;
import edu.univ.erp.ui.common.UITheme;
import edu.univ.erp.util.MessageUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class InstructorDashboard extends JFrame {
    private final Instructor instructor;
    private final InstructorService instructorService;
    private final AccessControl accessControl;

    private JTable sectionsTable;
    private JTable studentsTable;
    private JTable gradesSummaryTable;
    private JLabel maintenanceBanner;
    private Section selectedSection;
    private List<Enrollment> currentEnrollments;

    public InstructorDashboard(Instructor instructor) {
        this.instructor = instructor;
        this.instructorService = new InstructorService();
        this.accessControl = AccessControl.getInstance();

        initComponents();
        setupLayout();
        loadSections();
    }

    private void initComponents() {
        setTitle("University ERP - Instructor Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        maintenanceBanner = UITheme.createMaintenanceBanner();
        maintenanceBanner.setVisible(accessControl.isMaintenanceModeEnabled());
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BACKGROUND);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.PRIMARY);
        topPanel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_LG, UITheme.SPACING_MD, UITheme.SPACING_LG));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Instructor Dashboard");
        titleLabel.setFont(UITheme.FONT_SUBHEADER);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel userLabel = new JLabel("  •  " + instructor.getEmployeeId() + " - " + instructor.getDepartment());
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
        logoutButton.addActionListener(e -> logout());

        buttonPanel.add(changePasswordButton);
        buttonPanel.add(logoutButton);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBackground(UITheme.BACKGROUND);
        bannerPanel.add(maintenanceBanner, BorderLayout.CENTER);

        JPanel topArea = new JPanel(new BorderLayout());
        topArea.add(bannerPanel, BorderLayout.NORTH);
        topArea.add(topPanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createSectionsPanel());
        splitPane.setRightComponent(createDetailsPanel());
        splitPane.setDividerLocation(320);
        splitPane.setDividerSize(2);
        splitPane.setBorder(null);

        add(topArea, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void showChangePasswordDialog() {
        new edu.univ.erp.ui.common.ChangePasswordDialog(this).setVisible(true);
    }

    private JPanel createSectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(UITheme.createTitledBorder("My Sections"));

        String[] columns = {"Course", "Section", "Semester", "Enrolled"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        sectionsTable = new JTable(model);
        UITheme.styleTable(sectionsTable);
        sectionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSectionDetails();
            }
        });

        JScrollPane scrollPane = new JScrollPane(sectionsTable);
        UITheme.styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDetailsPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        UITheme.styleTabbedPane(tabbedPane);
        tabbedPane.addTab("Students", createStudentsPanel());
        tabbedPane.addTab("Grades", createGradesPanel());
        tabbedPane.addTab("Statistics", createStatsPanel());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.SURFACE);
        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.SURFACE);

        String[] columns = {"Student ID", "Roll No", "Program"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentsTable = new JTable(model);
        UITheme.styleTable(studentsTable);

        JScrollPane scrollPane = new JScrollPane(studentsTable);
        UITheme.styleScrollPane(scrollPane);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.SURFACE);

        String[] columns = {"Student ID", "Roll No", "Program", "Final Grade", "Percentage"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        gradesSummaryTable = new JTable(model);
        UITheme.styleTable(gradesSummaryTable);
        gradesSummaryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        gradesSummaryTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showStudentGradeDetails();
                }
            }
        });

        JButton viewDetailsButton = new JButton("View Grade Details");
        UITheme.styleSecondaryButton(viewDetailsButton);
        viewDetailsButton.addActionListener(e -> showStudentGradeDetails());

        JButton enterGradeButton = new JButton("Enter Grade");
        UITheme.stylePrimaryButton(enterGradeButton);
        enterGradeButton.addActionListener(e -> showEnterGradeDialog());

        JButton refreshButton = new JButton("↻ Refresh");
        UITheme.styleSecondaryButton(refreshButton);
        refreshButton.addActionListener(e -> loadGrades());

        JButton exportButton = new JButton("Export CSV");
        UITheme.styleSecondaryButton(exportButton);
        exportButton.addActionListener(e -> exportGradesToCSV());

        JButton exportPDFButton = new JButton("Export PDF");
        UITheme.styleSecondaryButton(exportPDFButton);
        exportPDFButton.addActionListener(e -> exportGradesToPDF());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, UITheme.SPACING_SM, UITheme.SPACING_SM));
        buttonPanel.setOpaque(false);
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(enterGradeButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(exportPDFButton);

        JLabel hintLabel = new JLabel("  Double-click on a student to see grade component details");
        hintLabel.setFont(UITheme.FONT_SMALL);
        hintLabel.setForeground(UITheme.TEXT_SECONDARY);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(hintLabel, BorderLayout.WEST);

        JScrollPane scrollPane = new JScrollPane(gradesSummaryTable);
        UITheme.styleScrollPane(scrollPane);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.SURFACE);

        JTextArea statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statsArea.setBackground(UITheme.SURFACE_DARK);
        statsArea.setForeground(UITheme.TEXT_PRIMARY);
        statsArea.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD));

        JButton showStatsButton = new JButton("Show Statistics");
        UITheme.stylePrimaryButton(showStatsButton);
        showStatsButton.addActionListener(e -> {
            if (selectedSection != null) {
                String stats = instructorService.getClassStatistics(selectedSection.getSectionId());
                statsArea.setText(stats);
            } else {
                MessageUtil.showWarning(this, "Please select a section first.");
            }
        });

        JScrollPane scrollPane = new JScrollPane(statsArea);
        UITheme.styleScrollPane(scrollPane);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(showStatsButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadSections() {
        DefaultTableModel model = (DefaultTableModel) sectionsTable.getModel();
        model.setRowCount(0);

        List<Section> sections = instructorService.getMySections(instructor.getInstructorId());
        for (Section section : sections) {
            model.addRow(new Object[]{
                section.getCourseCode() + " - " + section.getCourseTitle(),
                section.getSectionCode(),
                section.getSemester() + " " + section.getYear(),
                section.getEnrolledCount() + "/" + section.getCapacity()
            });
        }
    }

    private void loadSectionDetails() {
        int selectedRow = sectionsTable.getSelectedRow();
        if (selectedRow < 0) {
            selectedSection = null;
            return;
        }

        List<Section> sections = instructorService.getMySections(instructor.getInstructorId());
        if (selectedRow < sections.size()) {
            selectedSection = sections.get(selectedRow);
            loadStudents();
            loadGrades();
        }
    }

    private void loadStudents() {
        DefaultTableModel model = (DefaultTableModel) studentsTable.getModel();
        model.setRowCount(0);

        if (selectedSection == null) return;

        List<Enrollment> enrollments = instructorService.getSectionEnrollments(selectedSection.getSectionId());
        for (Enrollment enrollment : enrollments) {
            String rollNo = enrollment.getStudentRollNo() != null ? enrollment.getStudentRollNo() : "N/A";
            String program = enrollment.getStudentProgram() != null ? enrollment.getStudentProgram() : "N/A";
            model.addRow(new Object[]{
                enrollment.getStudentId(),
                rollNo,
                program
            });
        }
    }

    private void loadGrades() {
        DefaultTableModel model = (DefaultTableModel) gradesSummaryTable.getModel();
        model.setRowCount(0);

        if (selectedSection == null) return;

        currentEnrollments = instructorService.getSectionEnrollments(selectedSection.getSectionId());

        for (Enrollment enrollment : currentEnrollments) {
            String rollNo = enrollment.getStudentRollNo() != null ? enrollment.getStudentRollNo() : "N/A";
            String program = enrollment.getStudentProgram() != null ? enrollment.getStudentProgram() : "N/A";
            List<Grade> grades = instructorService.getGrades(enrollment.getEnrollmentId());

            String finalGrade;
            String percentage;

            if (grades.isEmpty()) {
                finalGrade = "No grades";
                percentage = "-";
            } else {
                String calculated = calculateStudentFinalGrade(grades);
                String[] parsed = parseGradeString(calculated);
                finalGrade = parsed[0];
                percentage = parsed[1];
            }

            model.addRow(new Object[]{
                enrollment.getStudentId(),
                rollNo,
                program,
                finalGrade,
                percentage
            });
        }
    }

    private String[] parseGradeString(String gradeStr) {
        if (gradeStr == null || gradeStr.equals("N/A") || !gradeStr.contains(" ") || !gradeStr.contains("(")) {
            return new String[]{"N/A", "-"};
        }
        try {
            String letterGrade = gradeStr.split(" ")[0];
            int openParen = gradeStr.indexOf("(");
            int closeParen = gradeStr.indexOf(")");
            if (openParen >= 0 && closeParen > openParen) {
                String percentage = gradeStr.substring(openParen + 1, closeParen);
                return new String[]{letterGrade, percentage};
            }
        } catch (Exception e) {

        }
        return new String[]{"N/A", "-"};
    }

    private String calculateStudentFinalGrade(List<Grade> grades) {
        if (grades.isEmpty()) {
            return "N/A";
        }

        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeightage = BigDecimal.ZERO;

        for (Grade grade : grades) {
            if (grade.getScore() != null && grade.getMaxScore() != null && grade.getWeightage() != null
                && grade.getMaxScore().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = grade.getScore()
                        .divide(grade.getMaxScore(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));

                BigDecimal weightedScore = percentage.multiply(grade.getWeightage())
                        .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

                totalWeightedScore = totalWeightedScore.add(weightedScore);
                totalWeightage = totalWeightage.add(grade.getWeightage());
            }
        }

        if (totalWeightage.compareTo(BigDecimal.ZERO) == 0) {
            return "N/A";
        }

        BigDecimal finalPercent = totalWeightedScore.setScale(2, RoundingMode.HALF_UP);
        String letterGrade = convertToLetterGrade(finalPercent);
        return String.format("%s (%.2f%%)", letterGrade, finalPercent);
    }

    private String convertToLetterGrade(BigDecimal score) {
        if (score.compareTo(new BigDecimal("95")) >= 0) return "A+";
        if (score.compareTo(new BigDecimal("90")) >= 0) return "A";
        if (score.compareTo(new BigDecimal("85")) >= 0) return "A-";
        if (score.compareTo(new BigDecimal("80")) >= 0) return "B+";
        if (score.compareTo(new BigDecimal("75")) >= 0) return "B";
        if (score.compareTo(new BigDecimal("70")) >= 0) return "B-";
        if (score.compareTo(new BigDecimal("65")) >= 0) return "C+";
        if (score.compareTo(new BigDecimal("60")) >= 0) return "C";
        if (score.compareTo(new BigDecimal("55")) >= 0) return "C-";
        if (score.compareTo(new BigDecimal("50")) >= 0) return "D";
        return "F";
    }

    private void showStudentGradeDetails() {
        int selectedRow = gradesSummaryTable.getSelectedRow();
        if (selectedRow < 0) {
            MessageUtil.showWarning(this, "Please select a student to view their grade details.");
            return;
        }

        if (currentEnrollments == null) {
            MessageUtil.showWarning(this, "Please select a section first.");
            return;
        }

        if (selectedRow >= currentEnrollments.size()) {
            MessageUtil.showWarning(this, "Invalid student selection. Please refresh and try again.");
            return;
        }

        Enrollment enrollment = currentEnrollments.get(selectedRow);
        String rollNo = enrollment.getStudentRollNo() != null ? enrollment.getStudentRollNo() : "N/A";
        String program = enrollment.getStudentProgram() != null ? enrollment.getStudentProgram() : "N/A";

        List<Grade> grades = instructorService.getGrades(enrollment.getEnrollmentId());

        JDialog dialog = new JDialog(this, "Grade Details - " + rollNo, true);
        dialog.setSize(650, 400);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel infoPanel = new JPanel(new MigLayout("wrap 2", "[][grow,fill]", "[]5[]5[]"));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Student Information"));
        infoPanel.add(new JLabel("Student ID:"));
        infoPanel.add(new JLabel(String.valueOf(enrollment.getStudentId())));
        infoPanel.add(new JLabel("Roll No:"));
        infoPanel.add(new JLabel(rollNo));
        infoPanel.add(new JLabel("Program:"));
        infoPanel.add(new JLabel(program));

        String finalGradeStr = calculateStudentFinalGrade(grades);
        JLabel finalGradeLabel = new JLabel(finalGradeStr);
        finalGradeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        finalGradeLabel.setForeground(new Color(0, 100, 0));
        infoPanel.add(new JLabel("Final Grade:"));
        infoPanel.add(finalGradeLabel);

        String[] columns = {"Grade ID", "Component", "Score", "Max Score", "Weightage (%)", "Percentage"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable detailsTable = new JTable(model);

        detailsTable.getColumnModel().getColumn(0).setMinWidth(0);
        detailsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        detailsTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        if (grades.isEmpty()) {
            model.addRow(new Object[]{0, "No grades entered yet", "-", "-", "-", "-"});
        } else {
            for (Grade grade : grades) {
                String percentage = "-";
                if (grade.getScore() != null && grade.getMaxScore() != null
                        && grade.getMaxScore().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal pct = grade.getScore()
                            .divide(grade.getMaxScore(), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                            .setScale(2, RoundingMode.HALF_UP);
                    percentage = pct + "%";
                }
                model.addRow(new Object[]{
                    grade.getGradeId(),
                    grade.getComponent(),
                    grade.getScore(),
                    grade.getMaxScore(),
                    grade.getWeightage(),
                    percentage
                });
            }
        }

        JScrollPane tableScroll = new JScrollPane(detailsTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Grade Components"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton editButton = new JButton("Edit Selected Grade");
        editButton.addActionListener(e -> {
            int detailRow = detailsTable.getSelectedRow();
            if (detailRow < 0) {
                MessageUtil.showWarning(dialog, "Please select a grade to edit.");
                return;
            }
            int gradeId = (int) detailsTable.getValueAt(detailRow, 0);
            if (gradeId == 0) {
                MessageUtil.showWarning(dialog, "No grade to edit. Use 'Enter Grade' from the main panel.");
                return;
            }
            dialog.dispose();
            showEditGradeDialogForGrade(gradeId, enrollment);
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(editButton);
        buttonPanel.add(closeButton);

        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void showEditGradeDialogForGrade(int gradeId, Enrollment enrollment) {

        List<Grade> grades = instructorService.getGrades(enrollment.getEnrollmentId());
        Grade targetGrade = null;
        for (Grade g : grades) {
            if (g.getGradeId() == gradeId) {
                targetGrade = g;
                break;
            }
        }

        if (targetGrade == null) {
            MessageUtil.showError(this, "Grade not found.");
            return;
        }

        String rollNo = enrollment.getStudentRollNo() != null ? enrollment.getStudentRollNo() : "N/A";

        JPanel panel = new JPanel(new MigLayout("wrap 2", "[][grow,fill]", "[]10[]10[]10[]10[]"));

        JLabel studentLabel = new JLabel("Student " + enrollment.getStudentId() + " (" + rollNo + ")");
        JLabel componentLabel = new JLabel(targetGrade.getComponent());
        JTextField scoreField = new JTextField(targetGrade.getScore() != null ? targetGrade.getScore().toString() : "");
        JTextField maxScoreField = new JTextField(targetGrade.getMaxScore() != null ? targetGrade.getMaxScore().toString() : "");
        JTextField weightageField = new JTextField(targetGrade.getWeightage() != null ? targetGrade.getWeightage().toString() : "");

        panel.add(new JLabel("Student:"));
        panel.add(studentLabel);
        panel.add(new JLabel("Component:"));
        panel.add(componentLabel);
        panel.add(new JLabel("Score:"));
        panel.add(scoreField);
        panel.add(new JLabel("Max Score:"));
        panel.add(maxScoreField);
        panel.add(new JLabel("Weightage (%):"));
        panel.add(weightageField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Grade",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                BigDecimal score = new BigDecimal(scoreField.getText().trim());
                BigDecimal maxScore = new BigDecimal(maxScoreField.getText().trim());
                BigDecimal weightage = new BigDecimal(weightageField.getText().trim());

                Grade grade = new Grade();
                grade.setGradeId(gradeId);
                grade.setScore(score);
                grade.setMaxScore(maxScore);
                grade.setWeightage(weightage);

                String message = instructorService.updateGrade(grade);

                if (message == null) {
                    MessageUtil.showSuccess(this, "Grade updated successfully.");
                    loadGrades();
                } else {
                    MessageUtil.showError(this, message);
                }
            } catch (Exception e) {
                MessageUtil.showError(this, "Invalid input: " + e.getMessage());
            }
        }
    }

    private void showEnterGradeDialog() {
        if (selectedSection == null) {
            MessageUtil.showWarning(this, "Please select a section first.");
            return;
        }

        List<Enrollment> enrollments = instructorService.getSectionEnrollments(selectedSection.getSectionId());
        if (enrollments.isEmpty()) {
            MessageUtil.showInfo(this, "No students enrolled in this section.");
            return;
        }

        JPanel panel = new JPanel(new MigLayout("wrap 2", "[][grow,fill]", "[]10[]10[]10[]10[]10[]10[]"));

        JComboBox<String> studentCombo = new JComboBox<>();
        for (Enrollment e : enrollments) {
            String rollNo = e.getStudentRollNo() != null ? e.getStudentRollNo() : "Student " + e.getStudentId();
            studentCombo.addItem(rollNo);
        }

        JLabel weightageInfoLabel = new JLabel();
        weightageInfoLabel.setFont(new Font("Arial", Font.ITALIC, 11));

        Runnable updateWeightageInfo = () -> {
            int idx = studentCombo.getSelectedIndex();
            if (idx >= 0 && idx < enrollments.size()) {
                Enrollment selected = enrollments.get(idx);
                BigDecimal total = instructorService.getTotalWeightage(selected.getEnrollmentId());
                BigDecimal remaining = new BigDecimal("100").subtract(total);
                String colorHtml = remaining.compareTo(BigDecimal.ZERO) <= 0 ? "red" :
                                   remaining.compareTo(new BigDecimal("20")) <= 0 ? "orange" : "green";
                weightageInfoLabel.setText(String.format(
                    "<html><span style='color:%s'>Current total: %.2f%% | Remaining: %.2f%%</span></html>",
                    colorHtml, total, remaining));
            }
        };
        updateWeightageInfo.run();
        studentCombo.addActionListener(e -> updateWeightageInfo.run());

        String[] prebuiltComponents = {
            "-- Select or type custom --",
            "Midsem Exam",
            "Endsem Exam",
            "Quiz 1",
            "Quiz 2",
            "Quiz 3",
            "Assignment 1",
            "Assignment 2",
            "Assignment 3",
            "Lab 1",
            "Lab 2",
            "Lab 3",
            "Lab Exam",
            "Project",
            "Presentation",
            "Participation"
        };
        JComboBox<String> componentCombo = new JComboBox<>(prebuiltComponents);
        componentCombo.setEditable(true);

        JTextField scoreField = new JTextField();
        JTextField maxScoreField = new JTextField("100");
        JTextField weightageField = new JTextField();

        componentCombo.addActionListener(e -> {
            String selected = (String) componentCombo.getSelectedItem();
            if (selected != null) {
                switch (selected) {
                    case "Midsem Exam":
                        maxScoreField.setText("100");
                        weightageField.setText("20");
                        break;
                    case "Endsem Exam":
                        maxScoreField.setText("100");
                        weightageField.setText("40");
                        break;
                    case "Quiz 1": case "Quiz 2": case "Quiz 3":
                        maxScoreField.setText("10");
                        weightageField.setText("5");
                        break;
                    case "Assignment 1": case "Assignment 2": case "Assignment 3":
                        maxScoreField.setText("100");
                        weightageField.setText("5");
                        break;
                    case "Lab 1": case "Lab 2": case "Lab 3":
                        maxScoreField.setText("50");
                        weightageField.setText("5");
                        break;
                    case "Lab Exam":
                        maxScoreField.setText("100");
                        weightageField.setText("10");
                        break;
                    case "Project":
                        maxScoreField.setText("100");
                        weightageField.setText("15");
                        break;
                    case "Presentation":
                        maxScoreField.setText("100");
                        weightageField.setText("5");
                        break;
                    case "Participation":
                        maxScoreField.setText("100");
                        weightageField.setText("5");
                        break;
                }
            }
        });

        panel.add(new JLabel("Student:"));
        panel.add(studentCombo);
        panel.add(new JLabel("Weightage Status:"));
        panel.add(weightageInfoLabel);
        panel.add(new JLabel("Component:"));
        panel.add(componentCombo);
        panel.add(new JLabel("Score:"));
        panel.add(scoreField);
        panel.add(new JLabel("Max Score:"));
        panel.add(maxScoreField);
        panel.add(new JLabel("Weightage (%):"));
        panel.add(weightageField);

        JLabel noteLabel = new JLabel("<html><small><i>Note: Total weightage for all components must not exceed 100%.<br>" +
                "Grade Slabs: A+(95-100), A(90-95), A-(85-90), B+(80-85), B(75-80), B-(70-75),<br>C+(65-70), C(60-65), C-(55-60), D(50-55), F(&lt;50)</i></small></html>");
        panel.add(noteLabel, "span 2");

        int result = JOptionPane.showConfirmDialog(this, panel, "Enter Grade",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int studentIndex = studentCombo.getSelectedIndex();
                Enrollment enrollment = enrollments.get(studentIndex);

                String component = ((String) componentCombo.getSelectedItem()).trim();
                if (component.isEmpty() || component.equals("-- Select or type custom --")) {
                    MessageUtil.showError(this, "Please select or enter a component name.");
                    return;
                }

                BigDecimal score = new BigDecimal(scoreField.getText().trim());
                BigDecimal maxScore = new BigDecimal(maxScoreField.getText().trim());
                BigDecimal weightage = new BigDecimal(weightageField.getText().trim());

                String message = instructorService.enterGrade(enrollment.getEnrollmentId(),
                        component, score, maxScore, weightage);

                if (message == null) {
                    MessageUtil.showSuccess(this, "Grade entered successfully.");
                    loadGrades();
                } else {
                    MessageUtil.showError(this, message);
                }
            } catch (NumberFormatException ex) {
                MessageUtil.showError(this, "Please enter valid numbers for score, max score, and weightage.");
            } catch (Exception e) {
                MessageUtil.showError(this, "Invalid input: " + e.getMessage());
            }
        }
    }

    private void exportGradesToCSV() {
        if (selectedSection == null) {
            MessageUtil.showWarning(this, "Please select a section first.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(selectedSection.getCourseCode() + "_grades.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile())) {

                writer.println("Roll No,Student,Component,Score,Max Score,Weightage (%),Final Grade,Final Percentage");

                List<Enrollment> enrollments = instructorService.getSectionEnrollments(selectedSection.getSectionId());
                for (Enrollment enrollment : enrollments) {
                    String rollNo = enrollment.getStudentRollNo() != null ? enrollment.getStudentRollNo() : "N/A";
                    List<Grade> grades = instructorService.getGrades(enrollment.getEnrollmentId());

                    String finalGradeStr = calculateStudentFinalGrade(grades);
                    String[] parsed = parseGradeString(finalGradeStr);
                    String letterGrade = parsed[0];
                    String percentage = parsed[1];

                    if (grades.isEmpty()) {
                        writer.printf("%s,Student %d,No grades yet,-,-,-,%s,%s%n",
                            rollNo,
                            enrollment.getStudentId(),
                            "N/A",
                            "N/A");
                    } else {
                        for (Grade grade : grades) {
                            writer.printf("%s,Student %d,%s,%s,%s,%s,%s,%s%n",
                                rollNo,
                                enrollment.getStudentId(),
                                grade.getComponent(),
                                grade.getScore() != null ? grade.getScore() : "",
                                grade.getMaxScore() != null ? grade.getMaxScore() : "",
                                grade.getWeightage() != null ? grade.getWeightage() : "",
                                letterGrade,
                                percentage);
                        }
                    }
                }

                MessageUtil.showSuccess(this, "Grades exported successfully to: " + fileChooser.getSelectedFile().getAbsolutePath());
            } catch (Exception e) {
                MessageUtil.showError(this, "Failed to export grades: " + e.getMessage());
            }
        }
    }

    private void exportGradesToPDF() {
        if (selectedSection == null) {
            MessageUtil.showWarning(this, "Please select a section first.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(selectedSection.getCourseCode() + "_grades.pdf"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                com.lowagie.text.Document document = new com.lowagie.text.Document();
                com.lowagie.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(fileChooser.getSelectedFile()));
                document.open();

                com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD);
                com.lowagie.text.Font normalFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);

                document.add(new com.lowagie.text.Paragraph("GRADE REPORT", titleFont));
                document.add(new com.lowagie.text.Paragraph(" "));
                document.add(new com.lowagie.text.Paragraph("Course: " + selectedSection.getCourseCode() + " - " + selectedSection.getCourseTitle(), headerFont));
                document.add(new com.lowagie.text.Paragraph("Section: " + selectedSection.getSectionCode(), normalFont));
                document.add(new com.lowagie.text.Paragraph("Semester: " + selectedSection.getSemester() + " " + selectedSection.getYear(), normalFont));
                document.add(new com.lowagie.text.Paragraph("Instructor: " + instructor.getEmployeeId(), normalFont));
                document.add(new com.lowagie.text.Paragraph(" "));

                com.lowagie.text.pdf.PdfPTable summaryTable = new com.lowagie.text.pdf.PdfPTable(4);
                summaryTable.setWidthPercentage(100);

                summaryTable.addCell(new com.lowagie.text.Phrase("Roll No", headerFont));
                summaryTable.addCell(new com.lowagie.text.Phrase("Student", headerFont));
                summaryTable.addCell(new com.lowagie.text.Phrase("Final Grade", headerFont));
                summaryTable.addCell(new com.lowagie.text.Phrase("Percentage", headerFont));

                List<Enrollment> enrollments = instructorService.getSectionEnrollments(selectedSection.getSectionId());
                for (Enrollment enrollment : enrollments) {
                    String rollNo = enrollment.getStudentRollNo() != null ? enrollment.getStudentRollNo() : "N/A";
                    List<Grade> grades = instructorService.getGrades(enrollment.getEnrollmentId());
                    String finalGrade = calculateStudentFinalGrade(grades);

                    summaryTable.addCell(new com.lowagie.text.Phrase(rollNo, normalFont));
                    summaryTable.addCell(new com.lowagie.text.Phrase("Student " + enrollment.getStudentId(), normalFont));

                    String letterGrade = finalGrade.split(" ")[0];
                    String percentage = finalGrade.contains("(") ? finalGrade.substring(finalGrade.indexOf("(") + 1, finalGrade.indexOf(")")) : "N/A";

                    summaryTable.addCell(new com.lowagie.text.Phrase(letterGrade, normalFont));
                    summaryTable.addCell(new com.lowagie.text.Phrase(percentage, normalFont));
                }

                document.add(summaryTable);

                document.add(new com.lowagie.text.Paragraph(" "));
                document.add(new com.lowagie.text.Paragraph("Grade Scale:", headerFont));
                document.add(new com.lowagie.text.Paragraph("A+ (95-100%) | A (90-94.99%) | A- (85-89.99%) | B+ (80-84.99%) | B (75-79.99%)", normalFont));
                document.add(new com.lowagie.text.Paragraph("B- (70-74.99%) | C+ (65-69.99%) | C (60-64.99%) | C- (55-59.99%) | D (50-54.99%) | F (<50%)", normalFont));

                document.close();

                MessageUtil.showSuccess(this, "Grades exported successfully to: " + fileChooser.getSelectedFile().getAbsolutePath());
            } catch (Exception e) {
                MessageUtil.showError(this, "Failed to export grades: " + e.getMessage());
            }
        }
    }

    private void logout() {
        if (MessageUtil.showConfirmation(this, "Are you sure you want to logout?")) {
            SessionManager.getInstance().logout();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
